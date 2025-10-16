package com.quivio.emvhandheldlib.builder

import com.quivio.emvhandheldlib.contract.ConfigFactory
import com.quivio.emvhandheldlib.models.TransType

/**
 * Builder class for creating DSI EMV transaction request XML strings.
 * 
 * This class is responsible for constructing properly formatted XML requests for various
 * EMV payment operations. It uses configuration data from a ConfigFactory to populate
 * common fields and generates unique identifiers for each transaction.
 * 
 * Key Features:
 * - XML request generation for multiple transaction types
 * - Configuration-based field population
 * - Unique invoice and reference number generation
 * - Support for both sandbox and production environments
 * 
 * The builder creates XML requests that conform to the DSI EMV protocol specifications
 * and can be sent to the payment processing system.
 * 
 * @param config The configuration factory containing merchant and device settings
 */
internal class DsiEMVRequestBuilder(config: ConfigFactory) {

    // Configuration fields extracted from the ConfigFactory
    /** The merchant identifier for payment processing */
    private val merchantID = config.merchantID
    
    /** The online merchant identifier for web-based transactions */
    private val onlineMerchantID = config.onlineMerchantID
    
    /** The operation mode - "CERT" for sandbox, "PROD" for production */
    private val operationMode = if (config.isSandBox) "CERT" else "PROD"
    
    /** The communication port number (fixed at 1) */
    private val comPort = 1
    
    /** The secure device name for terminal identification */
    private val secureDevice = config.secureDeviceName
    
    /** The POS package identifier */
    private val posPackageID = config.posPackageID

    // Network and user configuration
    /** The operator/user identifier for transaction tracing */
    private val userTrace = config.operatorID
    
    /** The IP address of the PIN pad device */
    private val pinPadIPAddress = config.pinPadIPAddress
    
    /** The port number for PIN pad communication */
    private val pinPadPort = config.pinPadPort

    // Transaction sequencing
    /** The sequence number for transaction ordering (fixed value) */
    private var sequenceNo = "0010010010"

    /**
     * Creates a unique invoice number for transaction identification.
     * 
     * This method generates a unique invoice number by combining the user trace
     * identifier with the current timestamp, ensuring each transaction has a
     * unique identifier.
     * 
     * @return A unique invoice number string in the format "userTrace-timestamp"
     */
    private fun createUniqueInvoiceNo() = "${userTrace}-${System.currentTimeMillis()}"

    /**
     * Builds a PIN pad reset request XML.
     * 
     * This method creates an XML request to reset the PIN pad device. The reset operation
     * clears any pending transactions and prepares the device for new operations.
     * 
     * @return A formatted XML string for PIN pad reset request
     */
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

    /**
     * Builds a card data collection request XML.
     * 
     * This method creates an XML request to collect card data from a prepaid card.
     * The request instructs the PIN pad to read track data from the inserted card.
     * 
     * @return A formatted XML string for card data collection request
     */
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

    /**
     * Builds an EMV sale transaction request XML.
     * 
     * This method creates an XML request for a standard EMV sale transaction.
     * It includes all necessary fields for processing a payment including amount,
     * merchant information, and transaction metadata.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     * @return A formatted XML string for EMV sale transaction request
     */
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

    /**
     * Builds an EMV recurring sale transaction request XML.
     * 
     * This method creates an XML request for a recurring EMV sale transaction.
     * It includes additional fields specific to recurring payments such as
     * recurring data and card holder ID for future transactions.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     * @return A formatted XML string for EMV recurring sale transaction request
     */
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

    /**
     * Builds an EMV parameter download request XML.
     * 
     * This method creates an XML request to download configuration parameters
     * from the payment processor. This is typically used to update terminal
     * settings and ensure proper configuration.
     * 
     * @return A formatted XML string for EMV parameter download request
     */
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

    /**
     * Builds a card replacement in recurring transaction request XML.
     * 
     * This method creates an XML request for replacing a card in a recurring
     * payment setup. It performs a zero-amount authorization to validate the
     * new card without charging it.
     * 
     * @return A formatted XML string for card replacement request
     */
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

    /**
     * Builds a client version request XML.
     * 
     * This method creates an XML request to retrieve version information about
     * the EMV client library. This is useful for debugging and compatibility
     * verification purposes.
     * 
     * @return A formatted XML string for client version request
     */
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