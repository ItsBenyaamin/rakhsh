package com.benyaamin.rakhsh.model

enum class ErrorType(val error: String) {
    BadRequest("Bad request"),
    UnAuthorizedError("UnAuthorized request"),
    PaymentRequired("Payment required"),
    ForbiddenError("You don't have access to this resource"),
    FileNotFoundError("File does not exist"),
    InternalServerError("Server error"),
    UnAvailableError("Server is not available"),
    GatewayTimeOutError("Couldn't connect to the server"),
    TimeOutError("Couldn't connect to the server"),
    UnknownHostError("Failed to find the host"),
    IOError("Failed to find the host"),
    Error("Unknown error occurred"),
}