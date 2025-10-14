package com.quivio.emvhandheldlib.builder

import com.quivio.emvhandheldlib.contract.ConfigFactory
import com.quivio.emvhandheldlib.models.TransType

internal class DsiEMVRequestBuilder(config: ConfigFactory) {

    private val merchantID = config.merchantID
    private val onlineMerchantID = config.onlineMerchantID
    private val operationMode = if (config.isSandBox) "CERT" else "PROD"
    private val comPort = 1
    private val secureDevice = config.secureDeviceName
    private val posPackageID = config.posPackageID

    private val userTrace = config.operatorID
    private val pinPadIPAddress = config.pinPadIPAddress
    private val pinPadPort = config.pinPadPort

    private var sequenceNo = "0010010010"

    private fun createUniqueInvoiceNo() = "${userTrace}-${System.currentTimeMillis()}"

    fun buildPinPadResetRequest(): String {
        return """
            <?xml version="1.0"?>
            <TStream>
            <Transaction>
            <OperationMode>${operationMode}</OperationMode>    
            <UseForms>Suppressed</UseForms> 
            <MerchantID>${merchantID}</MerchantID>
            <PinPadIpAddress>${pinPadIPAddress}</PinPadIpAddress>
            <PinPadIpPort>${pinPadPort}</PinPadIpPort>
            <POSPackageID>${posPackageID}</POSPackageID>
            <SecureDevice>${secureDevice}</SecureDevice>
            <SequenceNo>${sequenceNo}</SequenceNo>
            <TranCode>${TransType.EMVPadReset.name}</TranCode>
            </Transaction>
            </TStream>
    """.trimIndent()
    }

    fun buildCollectCardDataRequest(): String {
        return """<?xml version="1.0" ?>
              <TStream>
                <Transaction>
                <PinPadIpAddress>${pinPadIPAddress}</PinPadIpAddress>
                <PinPadIpPort>${pinPadPort}</PinPadIpPort>
                <SecureDevice>${secureDevice}</SecureDevice>
                <SequenceNo>${sequenceNo}</SequenceNo>
                <UserTrace>${userTrace}</UserTrace>
                <MerchantID>${merchantID}</MerchantID>
                <TranCode>${TransType.GetPrePaidStripeAllTracks.name}</TranCode>
                </Transaction>
            </TStream>
        """.trimIndent()
    }

    fun buildEMVSaleRequest(amount: String): String {
        return """<?xml version="1.0"?>
        <TStream>
        <Transaction>
        <OKAmount>Disallow</OKAmount>
        <PinPadIpAddress>${pinPadIPAddress}</PinPadIpAddress>
        <PinPadIpPort>${pinPadPort}</PinPadIpPort>
        <SequenceNo>${sequenceNo}</SequenceNo>
        <UserTrace>${userTrace}</UserTrace>
        <POSPackageID>${posPackageID}</POSPackageID>
        <OperationMode>${operationMode}</OperationMode>    
        <MerchantID>${merchantID}</MerchantID>
        <ProcessorToken>TokenRequested</ProcessorToken>
        <CollectData>CardholderName</CollectData>
        <SecureDevice>${secureDevice}</SecureDevice>
        <TranCode>${TransType.EMVSale.name}</TranCode>
        <Amount>
            <Purchase>${amount}</Purchase>
        </Amount>
        <InvoiceNo>${createUniqueInvoiceNo()}</InvoiceNo>
        <RefNo>${createUniqueInvoiceNo()}</RefNo>
        <Frequency>Recurring</Frequency>
        <RecordNo>RecordNumberRequested</RecordNo>
        </Transaction>
        </TStream>""".trimIndent()
    }

    fun buildEMVRecurringSaleRequest(amount: String): String {
        return """<?xml version="1.0"?>
        <TStream>
        <Transaction>
            <ComPort>${comPort}</ComPort>
            <PinPadIpAddress>${pinPadIPAddress}</PinPadIpAddress>
            <PinPadIpPort>${pinPadPort}</PinPadIpPort>
            <MerchantID>${merchantID}</MerchantID>
            <OperationMode>${operationMode}</OperationMode>
            <POSPackageID>${posPackageID}</POSPackageID>
            <UserTrace>${userTrace}</UserTrace>
            <CardType>Credit</CardType>
            <TranCode>${TransType.EMVSale.name}</TranCode>
            <ProcessorToken>TokenRequested</ProcessorToken>
            <CollectData>CardholderName</CollectData>
            <SecureDevice>${secureDevice}</SecureDevice>
            <InvoiceNo>${createUniqueInvoiceNo()}</InvoiceNo>
            <RefNo>${createUniqueInvoiceNo()}</RefNo>
            <Amount>
                <Purchase>${amount}</Purchase>
            </Amount>
            <SequenceNo>${sequenceNo}</SequenceNo>
            <Frequency>Recurring</Frequency>
            <RecurringData>Recurring</RecurringData>
            <RecordNo>RecordNumberRequested</RecordNo>
            <CardHolderID>Allow_V2</CardHolderID>
        </Transaction>
        </TStream>""".trimIndent()
    }

    fun buildEMVParamDownloadRequest(): String {
        return """
        <?xml version="1.0"?>
        <TStream>
        <Admin>
        <ComPort>${comPort}</ComPort>
        <PinPadIpAddress>${pinPadIPAddress}</PinPadIpAddress>
        <PinPadIpPort>${pinPadPort}</PinPadIpPort>
        <OperationMode>${operationMode}</OperationMode>    
        <TranCode>${TransType.EMVParamDownload.name}</TranCode>
        <MerchantID>${merchantID}</MerchantID>
        <SequenceNo>${sequenceNo}</SequenceNo>
        <POSPackageID>${posPackageID}</POSPackageID>
        <SecureDevice>${secureDevice}</SecureDevice>
        </Admin>
        </TStream>
    """.trimIndent()
    }

    fun buildReplaceCardInRecurringRequest(): String {
        return """<?xml version="1.0"?>
        <TStream>
        <Transaction>
            <PinPadIpAddress>${pinPadIPAddress}</PinPadIpAddress>
            <PinPadIpPort>${pinPadPort}</PinPadIpPort>
            <OperationMode>${operationMode}</OperationMode>
            <MerchantID>${merchantID}</MerchantID>
            <POSPackageID>${posPackageID}</POSPackageID>
            <UserTrace>${userTrace}</UserTrace>
            <TranCode>${TransType.EMVZeroAuth.name}</TranCode>
            <ProcessorToken>TokenRequested</ProcessorToken>
            <SecureDevice>${secureDevice}</SecureDevice>
            <InvoiceNo>${createUniqueInvoiceNo()}</InvoiceNo>
            <RefNo>${createUniqueInvoiceNo()}</RefNo>
            <Amount>
                <Purchase>0.00</Purchase>
            </Amount>
            <SequenceNo>${sequenceNo}</SequenceNo>
            <CollectData>CardholderName</CollectData>
            <RecordNo>RecordNumberRequested</RecordNo>
            <Frequency>Frequency</Frequency>
            <CardHolderID>Allow_V2</CardHolderID>
        </Transaction>
        </TStream>""".trimIndent()
    }

    fun buildClientVersionRequest(): String {
        return """<?xml version="1.0"?>
        <TStream>
        <Admin>
            <OperationMode>${operationMode}</OperationMode>
            <PinPadIpAddress>${pinPadIPAddress}</PinPadIpAddress>
            <PinPadIpPort>${pinPadPort}</PinPadIpPort>
            <MerchantID>${merchantID}</MerchantID>
            <UserTrace>${userTrace}</UserTrace>
            <POSPackageID>${posPackageID}</POSPackageID>
            <TranCode>${TransType.ClientVersion.name}</TranCode>
            <SecureDevice>${secureDevice}</SecureDevice>
            <SequenceNo>${sequenceNo}</SequenceNo>
        </Admin>
        </TStream>""".trimIndent()
    }
}