use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use radiogolha_core::RadioGolhaCore;
use serde_json::{json, to_string};

fn categories_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let categories = core.program_categories().map_err(|error| error.to_string())?;
    to_string(&categories).map_err(|error| error.to_string())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getCategoriesJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    let payload = match env.get_string(&db_path) {
        Ok(path) => match categories_json(&path.to_string_lossy()) {
            Ok(json) => json,
            Err(error) => json!({ "error": error }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    };

    match env.new_string(payload) {
        Ok(value) => value.into_raw(),
        Err(error) => env
            .new_string(json!({ "error": error.to_string() }).to_string())
            .expect("fallback JNI string should be created")
            .into_raw(),
    }
}
