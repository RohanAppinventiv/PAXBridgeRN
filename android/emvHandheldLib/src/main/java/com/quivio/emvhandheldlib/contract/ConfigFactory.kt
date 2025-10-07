package com.quivio.emvhandheldlib.contract

interface ConfigFactory {
    val merchantID: String
    val onlineMerchantID: String
    val isSandBox: Boolean
    val secureDeviceName: String
    val operatorID: String
    val posPackageID: String
    val pinPadIPAddress: String
    val pinPadPort: String
}