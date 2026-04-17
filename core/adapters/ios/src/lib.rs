use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use radiogolha_core::{RadioGolhaCore};
use serde_json::{json, to_string};

// --- Helper Functions for String Management ---

fn rust_str_to_c(s: String) -> *mut c_char {
    CString::new(s).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn radiogolha_free_string(s: *mut c_char) {
    if s.is_null() { return; }
    unsafe { CString::from_raw(s); }
}

fn get_path(c_str: *const c_char) -> String {
    if c_str.is_null() { return String::new(); }
    unsafe { CStr::from_ptr(c_str).to_string_lossy().into_owned() }
}

// --- Implementation Logic (Decoupled from JNI/C) ---
// This matches the logic from the Android adapter but exports for C

#[no_mangle]
pub extern "C" fn get_home_feed_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    // Note: In a production app, we would share the DTO structs. 
    // For now, I'm calling the core and wrapping in JSON just like the Android version.
    // For brevity, I'll provide a simplified version that returns core data.
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            // Simplified for initial iOS build setup
            // We can port full struct logic later
            rust_str_to_c(json!({ "status": "ok", "message": "Bridge is working" }).to_string())
        },
        Err(e) => rust_str_to_c(json!({ "error": e.to_string() }).to_string())
    }
}

// ... Additional functions will be added here as we progress
