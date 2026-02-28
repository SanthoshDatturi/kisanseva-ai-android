package com.kisanseva.ai.data.remote

import com.kisanseva.ai.domain.error.DataError

fun Int.toNetworkError(): DataError.Network {
    return when (this) {
        400 -> DataError.Network.USER_NOT_FOUND // Adjust mapping based on specific API documentation if needed
        401 -> DataError.Network.UNAUTHORIZED
        403 -> DataError.Network.FORBIDDEN
        404 -> DataError.Network.NOT_FOUND
        408 -> DataError.Network.REQUEST_TIMEOUT
        409 -> DataError.Network.CONFLICT
        413 -> DataError.Network.PAYLOAD_TOO_LARGE
        429 -> DataError.Network.TOO_MANY_REQUESTS
        in 500..599 -> DataError.Network.SERVER_ERROR
        else -> DataError.Network.UNKNOWN
    }
}
