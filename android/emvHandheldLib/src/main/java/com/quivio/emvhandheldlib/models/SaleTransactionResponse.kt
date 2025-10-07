package com.quivio.emvhandheldlib.models


data class SaleTransactionResponse(
    // CmdResponse section
    val responseOrigin: String,
    val dsixReturnCode: String,
    val cmdStatus: String,
    val textResponse: String,

    // TranResponse section
    val merchantID: String,
    val payAPIId: String,
    val acctNo: String,
    val cardType: String,
    val tranCode: String,
    val authCode: String,
    val avsResult: String,
    val cvvResult: String,
    val captureStatus: String,
    val refNo: String,
    val amount: Amount,
    val cardholderName: String,
    val acqRefData: String,
    val processorToken: String,
    val processData: String,
    val recordNo: String,
    val recurringData: String,
    val entryMethod: String,
    val date: String,
    val time: String,
    val cvm: String
)