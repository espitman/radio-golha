use std::path::PathBuf;

use thiserror::Error;

#[derive(Debug, Error)]
pub enum CoreError {
    #[error("database file not found: {0}")]
    DatabaseNotFound(PathBuf),
    #[error("database error: {0}")]
    Database(#[from] rusqlite::Error),
}

pub type CoreResult<T> = Result<T, CoreError>;
