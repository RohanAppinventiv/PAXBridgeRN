package com.quivio.emvhandheldlib.models

data class CardData(
    // CmdResponse section
    val responseOrigin: String,
    val dsixReturnCode: String,
    val cmdStatus: String,
    val textResponse: String,

    // TranResponse section
    val track1Status: String,
    val track2Status: String,
    val track1Data: String?,
    val track2Data: String?,
)