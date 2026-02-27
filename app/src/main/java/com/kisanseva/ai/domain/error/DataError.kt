package com.kisanseva.ai.domain.error

sealed interface DataError: RootError {
    enum class Network: DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
        NOT_FOUND,
        UNAUTHORIZED,
        FORBIDDEN
    }
    enum class Local: DataError {
        DISK_FULL,
        PERMISSION_DENIED
    }
}