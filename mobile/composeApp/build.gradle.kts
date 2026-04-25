import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val androidSdkRoot = providers
    .environmentVariable("ANDROID_SDK_ROOT")
    .orElse(providers.environmentVariable("ANDROID_HOME"))
    .orElse("${System.getProperty("user.home")}/Library/Android/sdk")

val ndkRoot = androidSdkRoot.map { "$it/ndk/27.1.12297006" }
val ndkPrebuiltDir = ndkRoot.map { "$it/toolchains/llvm/prebuilt/darwin-x86_64" }
val rustAdapterDir = rootProject.file("../core/adapters/android")
val rustIosAdapterDir = rootProject.file("../core/adapters/ios")
val rustApiLevel = "24"
val rustLibName = "libradiogolha_android.so"
val rustJniLibsRootDir = layout.buildDirectory.dir("generated/rust/jniLibs")
val archiveAssetsDir = layout.buildDirectory.dir("generated/archive-assets")
val androidRustTargets = listOf(
    AndroidRustTarget(
        triple = "aarch64-linux-android",
        abi = "arm64-v8a",
        linkerPrefix = "aarch64-linux-android",
        cargoEnvKey = "AARCH64_LINUX_ANDROID",
        ccEnvKey = "aarch64_linux_android",
    ),
    AndroidRustTarget(
        triple = "armv7-linux-androideabi",
        abi = "armeabi-v7a",
        linkerPrefix = "armv7a-linux-androideabi",
        cargoEnvKey = "ARMV7_LINUX_ANDROIDEABI",
        ccEnvKey = "armv7_linux_androideabi",
    ),
)

data class AndroidRustTarget(
    val triple: String,
    val abi: String,
    val linkerPrefix: String,
    val cargoEnvKey: String,
    val ccEnvKey: String,
)
val releaseSigningPropertiesFile = rootProject.file("signing/release-signing.properties")
val releaseSigningProperties = Properties().apply {
    if (releaseSigningPropertiesFile.exists()) {
        releaseSigningPropertiesFile.inputStream().use(::load)
    }
}
val hasReleaseSigningProperties =
    releaseSigningProperties.getProperty("storeFile").orEmpty().isNotBlank() &&
        releaseSigningProperties.getProperty("storePassword").orEmpty().isNotBlank() &&
        releaseSigningProperties.getProperty("keyAlias").orEmpty().isNotBlank()

val syncArchiveDb by tasks.registering(Copy::class) {
    from(rootProject.file("../database/golha_database.db"))
    into(archiveAssetsDir)
}

val buildRustAndroid by tasks.registering {
    dependsOn(syncArchiveDb)

    doFirst {
        rustJniLibsRootDir.get().asFile.mkdirs()
    }

    doLast {
        androidRustTargets.forEach { target ->
            val clang = "${ndkPrebuiltDir.get()}/bin/${target.linkerPrefix}${rustApiLevel}-clang"
            exec {
                workingDir = rustAdapterDir
                environment("ANDROID_NDK_HOME", ndkRoot.get())
                environment("CARGO_TARGET_${target.cargoEnvKey}_LINKER", clang)
                environment("CC_${target.ccEnvKey}", clang)
                environment("AR_${target.ccEnvKey}", "${ndkPrebuiltDir.get()}/bin/llvm-ar")
                commandLine("cargo", "build", "--target", target.triple)
            }
            copy {
                from(File(rustAdapterDir, "target/${target.triple}/debug/$rustLibName"))
                into(rustJniLibsRootDir.get().dir(target.abi).asFile)
            }
        }
    }
}

val buildRustIos by tasks.registering {
    dependsOn(syncArchiveDb)

    doLast {
        val targets = listOf(
            "aarch64-apple-ios",      // real iPhone devices
            "aarch64-apple-ios-sim",  // Apple Silicon simulators
            "x86_64-apple-ios"        // Intel simulators
        )
        targets.forEach { target ->
            exec {
                workingDir = rustIosAdapterDir
                commandLine("cargo", "build", "--target", target)
            }
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.compilations.getByName("main") {
            val radiogolha_ios by cinterops.creating {
                definitionFile = project.file("src/nativeInterop/cinterop/radiogolha_ios.def")
                packageName = "com.radiogolha.mobile.native"
                includeDirs(rustIosAdapterDir)
            }
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "RadioGolhaMobile"
            isStatic = false
            
            val target = when(konanTarget.name) {
                "ios_x64" -> "x86_64-apple-ios"
                "ios_arm64" -> "aarch64-apple-ios"
                "ios_simulator_arm64" -> "aarch64-apple-ios-sim"
                else -> null
            }
            if (target != null) {
                linkerOpts("-L${rustIosAdapterDir}/target/${target}/debug", "-lradiogolha_ios")
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
            implementation(libs.coil.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.radiogolha.mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.radiogolha.mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
        manifestPlaceholders["appLabel"] = "@string/app_name"
    }

    sourceSets["main"].assets.srcDir(archiveAssetsDir)
    sourceSets["main"].jniLibs.srcDir(rustJniLibsRootDir)

    buildFeatures {
        compose = true
    }

    signingConfigs {
        if (hasReleaseSigningProperties) {
            create("release") {
                storeFile = file(releaseSigningProperties.getProperty("storeFile"))
                storePassword = releaseSigningProperties.getProperty("storePassword")
                keyAlias = releaseSigningProperties.getProperty("keyAlias")
                keyPassword = releaseSigningProperties.getProperty("keyPassword")
                    ?: releaseSigningProperties.getProperty("storePassword")
            }
        }
    }

    flavorDimensions += "device"
    productFlavors {
        create("mobile") {
            dimension = "device"
        }
        create("tv") {
            dimension = "device"
            applicationIdSuffix = ".tv"
            versionNameSuffix = "-tv"
            manifestPlaceholders["appLabel"] = "رادیو گل‌ها TV"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets["mobile"].manifest.srcFile("src/mobile/AndroidManifest.xml")
    sourceSets["tv"].manifest.srcFile("src/androidTv/AndroidManifest.xml")
}

dependencies {
    debugImplementation(compose.uiTooling)
}

tasks.matching {
    it.name in setOf(
        "mergeDebugJniLibFolders",
        "mergeDebugNativeLibs",
        "mergeDebugAssets",
        "mergeReleaseJniLibFolders",
        "mergeReleaseNativeLibs",
        "mergeReleaseAssets",
        "mergeMobileDebugJniLibFolders",
        "mergeMobileDebugNativeLibs",
        "mergeMobileDebugAssets",
        "mergeMobileReleaseJniLibFolders",
        "mergeMobileReleaseNativeLibs",
        "mergeMobileReleaseAssets",
        "mergeTvDebugJniLibFolders",
        "mergeTvDebugNativeLibs",
        "mergeTvDebugAssets",
        "mergeTvReleaseJniLibFolders",
        "mergeTvReleaseNativeLibs",
        "mergeTvReleaseAssets",
        "lintVitalAnalyzeRelease",
        "lintVitalAnalyzeTvRelease",
        "generateReleaseLintVitalReportModel",
        "generateTvReleaseLintVitalReportModel",
        "generateDebugLintModel",
        "generateReleaseLintModel",
        "generateTvReleaseLintModel"
    )
}
    .configureEach {
        dependsOn(buildRustAndroid)
        dependsOn(syncArchiveDb)
    }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
    dependsOn(buildRustIos)
}
