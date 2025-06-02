package com.benyaamin.rakhsh.util

import com.benyaamin.rakhsh.model.ErrorType
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Int.mapToErrorType(): ErrorType {
    return when(this) {
        400 -> ErrorType.BadRequest
        401 -> ErrorType.UnAuthorizedError
        402 -> ErrorType.PaymentRequired
        403 -> ErrorType.ForbiddenError
        404 -> ErrorType.FileNotFoundError
        500 -> ErrorType.InternalServerError
        503 -> ErrorType.UnAvailableError
        504 -> ErrorType.GatewayTimeOutError
        else -> ErrorType.Error
    }
}

fun Exception.mapToErrorType(): ErrorType {
    return when(this) {
        is SocketTimeoutException -> ErrorType.TimeOutError
        is UnknownHostException -> ErrorType.UnknownHostError
        else -> ErrorType.IOError
    }
}