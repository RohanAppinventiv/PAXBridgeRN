package com.quivio.emvhandheldlib.models

data class ErrorResponse(
    // Basic error response fields
    val responseOrigin: String,
    val dsixReturnCode: String,
    val cmdStatus: String,
    val textResponse: String,
    val sequenceNo: String,
    val userTrace: String
)