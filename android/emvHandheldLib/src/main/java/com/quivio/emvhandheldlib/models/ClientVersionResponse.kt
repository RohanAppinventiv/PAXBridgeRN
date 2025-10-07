package com.quivio.emvhandheldlib.models


data class ClientVersionResponse(
    // Basic response fields
    val responseOrigin: String,
    val dsixReturnCode: String,
    val cmdStatus: String,
    val textResponse: String,
    val tranCode: String,

    // Client version specific fields
    val clientVersion: String? = null,
    val clientLibraryName: String? = null
)