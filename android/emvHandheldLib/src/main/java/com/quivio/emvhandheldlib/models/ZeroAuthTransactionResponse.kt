package com.quivio.emvhandheldlib.models


data class ZeroAuthTransactionResponse(
    // Basic response fields
    val responseOrigin: String,
    val dsixReturnCode: String,
    val cmdStatus: String,
    val textResponse: String,
    val sequenceNo: String,
    val userTrace: String,

    // Transaction details
    val merchantID: String,
    val acctNo: String,
    val cardType: String,
    val tranCode: String,
    val authCode: String,
    val refNo: String,
    val invoiceNo: String,

    // Amount fields
    val amount: Amount,

    // Additional transaction data
    val acqRefData: String,
    val processData: String,
    val cardHolderID: String,
    val recordNo: String,
    val cardholderName: String,
    val entryMethod: String,
    val date: String,
    val time: String,
    val applicationLabel: String,

    // EMV specific fields
    val aid: String,
    val tvr: String,
    val iad: String,
    val tsi: String,
    val arc: String,
    val cvm: String,
    val payAPIId: String
)
