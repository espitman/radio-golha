import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val androidSdkRoot = providers
    .environmentVariable("ANDROID_SDK_ROOT")
    .orElse(providers.environmentVariable("ANDROID_HOME"))
    .orElse("${System.getProperty("user.home")}/Library/Android/sdk")

val ndkRoot = androidSdkRoot.map { "$it/ndk/27.1.12297006" }
val ndkPrebuiltDir = ndkRoot.map { "$it/toolchains/llvm/prebuilt/darwin-x86_64" }
val rustAdapterDir = rootProject.file("../core/adapters/android")
val rustTarget = "aarch64-linux-android"
val rustApiLevel = "24"
val rustLibName = "libradiogolha_android.so"
val rustJniLibsRootDir = layout.buildDirectory.dir("generated/rust/jniLibs")
val rustJniLibsDir = layout.buildDirectory.dir("generated/rust/jniLibs/arm64-v8a")
val archiveAssetsDir = layout.buildDirectory.dir("generated/archive-assets")

val syncArchiveDb by tasks.registering(Copy::class) {
    from(rootProject.file("../database/golha_database.db"))
    into(archiveAssetsDir)
}

val buildRustAndroid by tasks.registering(Exec::class) {
    dependsOn(syncArchiveDb)
    workingDir = rustAdapterDir

    doFirst {
        rustJniLibsDir.get().asFile.mkdirs()
    }

    environment("ANDROID_NDK_HOME", ndkRoot.get())
    environment("CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER", "${ndkPrebuiltDir.get()}/bin/aarch64-linux-android${rustApiLevel}-clang")
    environment("CC_aarch64_linux_android", "${ndkPrebuiltDir.get()}/bin/aarch64-linux-android${rustApiLevel}-clang")
    environment("AR_aarch64_linux_android", "${ndkPrebuiltDir.get()}/bin/llvm-ar")
    commandLine("cargo", "build", "--target", rustTarget)

    doLast {
        copy {
            from(File(rustAdapterDir, "target/$rustTarget/debug/$rustLibName"))
            into(rustJniLibsDir.get().asFile)
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "RadioGolhaMobile"
            isStatic = true
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
    }

    sourceSets["main"].assets.srcDir(archiveAssetsDir)
    sourceSets["main"].jniLibs.srcDir(rustJniLibsRootDir)

    buildFeatures {
        compose = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
        "lintVitalAnalyzeRelease",
        "generateReleaseLintVitalReportModel",
        "generateDebugLintModel",
        "generateReleaseLintModel"
    )
}
    .configureEach {
        dependsOn(buildRustAndroid)
        dependsOn(syncArchiveDb)
    }
