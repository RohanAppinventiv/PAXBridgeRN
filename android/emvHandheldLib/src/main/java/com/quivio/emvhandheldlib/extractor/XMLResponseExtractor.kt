package com.quivio.emvhandheldlib.extractor

import android.util.Log
import com.quivio.emvhandheldlib.models.Amount
import com.quivio.emvhandheldlib.models.CardData
import com.quivio.emvhandheldlib.models.ClientVersionResponse
import com.quivio.emvhandheldlib.models.ErrorResponse
import com.quivio.emvhandheldlib.models.RecurringTransactionResponse
import com.quivio.emvhandheldlib.models.SaleTransactionResponse
import com.quivio.emvhandheldlib.models.ZeroAuthTransactionResponse

/**
 * Utility class for extracting structured data from XML response strings.
 * 
 * This class provides methods to parse XML responses from the DSI EMV library
 * and convert them into strongly-typed Kotlin data classes. It handles various
 * types of responses including transaction results, error responses, and
 * configuration data.
 * 
 * Key Features:
 * - XML tag extraction using regex patterns
 * - Type-safe conversion to data models
 * - Error handling and fallback mechanisms
 * - Support for multiple response types
 * 
 * The extractor uses regex patterns to find and extract values from XML tags,
 * providing a robust way to parse the structured response data.
 */
class XMLResponseExtractor {

    companion object {
        /** Log tag for debugging purposes */
        private const val TAG = "XMLResponseExtractor"
    }

    /**
     * Extracts the inner text content of an XML tag.
     * 
     * This private helper method uses regex to find and extract the text content
     * between opening and closing XML tags. It handles case-insensitive matching
     * and supports multiline content.
     * 
     * @param xml The XML string to search in
     * @param tagName The name of the tag to extract (without angle brackets)
     * @return The inner text content of the tag, or null if the tag is not found
     */
    private fun getTag(xml: String, tagName: String): String? {
        // (?s) ⇒ dot matches newline
        val regex = Regex("(?s)<$tagName>(.*?)</$tagName>", RegexOption.IGNORE_CASE)
        return regex.find(xml)?.groupValues?.get(1)
    }

    /**
     * Extracts sale transaction response data from XML.
     * 
     * This method parses XML response data and converts it into a SaleTransactionResponse
     * object containing all the transaction details from a successful sale operation.
     * It handles both command response fields and transaction response fields.
     * 
     * @param xml The XML response string from the DSI EMV library
     * @return A SaleTransactionResponse object with extracted data, or null if parsing fails
     */
    fun extractSaleResponse(xml: String): SaleTransactionResponse? = try {
        // helper to fetch a tag or default to ""
        val tag: (String) -> String = { name -> getTag(xml, name) ?: "" }

        // build the amount object
        val amount = Amount(
            purchase  = tag("Purchase").ifEmpty { "0.00" },
            authorize = tag("Authorize").ifEmpty { "0.00" },
        )

        SaleTransactionResponse(
            // CmdResponse section
            responseOrigin   = tag("ResponseOrigin"),
            dsixReturnCode   = tag("DSIXReturnCode"),
            cmdStatus        = tag("CmdStatus"),
            textResponse     = tag("TextResponse"),

            // TranResponse section
            merchantID       = tag("MerchantID"),
            payAPIId         = tag("PayAPI_Id"),
            acctNo           = tag("AcctNo"),
            cardType         = tag("CardType"),
            tranCode         = tag("TranCode"),
            authCode         = tag("AuthCode"),
            avsResult        = tag("AVSResult"),
            cvvResult        = tag("CVVResult"),
            captureStatus    = tag("CaptureStatus"),
            refNo            = tag("RefNo"),
            amount           = amount,
            cardholderName   = tag("CardholderName"),
            acqRefData       = tag("AcqRefData"),
            processorToken   = tag("ProcessorToken"),
            processData      = tag("ProcessData"),
            recordNo         = tag("RecordNo"),
            recurringData    = tag("RecurringData"),
            entryMethod      = tag("EntryMethod"),
            date             = tag("Date"),
            time             = tag("Time"),
            cvm              = tag("CVM"),
        ).also {
            Log.d(TAG, "Successfully extracted SaleTransactionResponse from XML")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error extracting sale response: ${e.message}")
        // fall back to an "empty" response
        null
    }

    /**
     * Extracts recurring transaction response data from XML.
     * 
     * This method parses XML response data and converts it into a RecurringTransactionResponse
     * object containing all the transaction details from a successful recurring sale operation.
     * It extracts the same fields as a regular sale but is specifically for recurring payments.
     * 
     * @param xml The XML response string from the DSI EMV library
     * @return A RecurringTransactionResponse object with extracted data, or null if parsing fails
     */
    fun extractRecurringTransactionResponse(xml: String): RecurringTransactionResponse? = try {
        // helper to fetch a tag or default to ""
        val tag: (String) -> String = { name -> getTag(xml, name) ?: "" }

        val amount = Amount(
            purchase  = tag("Purchase").ifEmpty { "0.00" },
            authorize = tag("Authorize").ifEmpty { "0.00" },
        )

        // construct the comprehensive response
        RecurringTransactionResponse(
            // CmdResponse section
            responseOrigin   = tag("ResponseOrigin"),
            dsixReturnCode   = tag("DSIXReturnCode"),
            cmdStatus        = tag("CmdStatus"),
            textResponse     = tag("TextResponse"),

            // TranResponse section
            merchantID       = tag("MerchantID"),
            payAPIId         = tag("PayAPI_Id"),
            acctNo           = tag("AcctNo"),
            cardType         = tag("CardType"),
            tranCode         = tag("TranCode"),
            authCode         = tag("AuthCode"),
            avsResult        = tag("AVSResult"),
            cvvResult        = tag("CVVResult"),
            captureStatus    = tag("CaptureStatus"),
            refNo            = tag("RefNo"),
            amount           = amount,
            cardholderName   = tag("CardholderName"),
            acqRefData       = tag("AcqRefData"),
            processorToken   = tag("ProcessorToken"),
            processData      = tag("ProcessData"),
            recordNo         = tag("RecordNo"),
            recurringData    = tag("RecurringData"),
            entryMethod      = tag("EntryMethod"),
            date             = tag("Date"),
            time             = tag("Time"),
            cvm              = tag("CVM")
        ).also {
            Log.d(TAG, "Successfully extracted ComprehensiveTransactionResponse from XML")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error extracting comprehensive response: ${e.message}")
        // fall back to an "empty" response
        null
    }

    /**
     * Extracts zero auth transaction response data from XML.
     * 
     * This method parses XML response data and converts it into a ZeroAuthTransactionResponse
     * object containing all the transaction details from a successful card replacement
     * operation. It includes EMV-specific fields and transaction metadata.
     * 
     * @param xml The XML response string from the DSI EMV library
     * @return A ZeroAuthTransactionResponse object with extracted data, or null if parsing fails
     */
    fun extractZeroAuthResponse(xml: String): ZeroAuthTransactionResponse? = try {
        // helper to fetch a tag or default to ""
        val tag: (String) -> String = { name -> getTag(xml, name) ?: "" }

        val amount = Amount(
            purchase  = tag("Purchase").ifEmpty { "0.00" },
            authorize = tag("Authorize").ifEmpty { "0.00" }
        )
        // construct the comprehensive response
        ZeroAuthTransactionResponse(
            // Basic response fields
            responseOrigin   = tag("ResponseOrigin"),
            dsixReturnCode  = tag("DSIXReturnCode"),
            cmdStatus       = tag("CmdStatus"),
            textResponse    = tag("TextResponse"),
            sequenceNo      = tag("SequenceNo"),
            userTrace       = tag("UserTrace"),

            // Transaction details
            merchantID      = tag("MerchantID"),
            acctNo          = tag("AcctNo"),
            cardType        = tag("CardType"),
            tranCode        = tag("TranCode"),
            authCode        = tag("AuthCode"),
            refNo           = tag("RefNo"),
            invoiceNo       = tag("InvoiceNo"),

            // Amount fields
            amount         = amount,

            // Additional transaction data
            acqRefData      = tag("AcqRefData"),
            processData     = tag("ProcessData"),
            cardHolderID    = tag("CardHolderID"),
            recordNo        = tag("RecordNo"),
            cardholderName  = tag("CardholderName"),
            entryMethod     = tag("EntryMethod"),
            date            = tag("Date"),
            time            = tag("Time"),
            applicationLabel= tag("ApplicationLabel"),

            // EMV specific fields
            aid             = tag("AID"),
            tvr             = tag("TVR"),
            iad             = tag("IAD"),
            tsi             = tag("TSI"),
            arc             = tag("ARC"),
            cvm             = tag("CVM"),
            payAPIId        = tag("PayAPI_Id")
        ).also {
            Log.d(TAG, "Successfully extracted ComprehensiveTransactionResponse from XML")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error extracting comprehensive response: ${e.message}")
        // fall back to an "empty" response
        null
    }

    /**
     * Checks if the response indicates a process is already running.
     * 
     * This method examines the XML response to determine if another process
     * is already running on the EMV device. This is indicated by specific
     * response origin and return code values.
     * 
     * @param xml The XML response string to check
     * @return true if a process is already running, false otherwise
     */
    fun isProcessAlreadyRunning(xml: String): Boolean {
        return getTag(xml, "ResponseOrigin") == "Client" &&
                getTag(xml, "DSIXReturnCode") == "003002"
    }

    /**
     * Checks if the response indicates a transaction failure.
     * 
     * This method examines the XML response to determine if the transaction
     * failed. It checks for error status indicators in the command status
     * or capture status fields.
     * 
     * @param xml The XML response string to check
     * @return true if the transaction failed, false otherwise
     */
    fun isFailed(xml: String): Boolean {
        return getTag(xml, "CmdStatus") == "Error" || getTag(xml, "CaptureStatus") == "Declined"
    }

    /**
     * Extracts error response data from XML.
     * 
     * This method parses XML response data and converts it into an ErrorResponse
     * object containing all the error details from a failed operation.
     * It extracts error codes, messages, and other diagnostic information.
     * 
     * @param response The XML response string containing error information
     * @return An ErrorResponse object with extracted error data, or null if parsing fails
     */
    fun resolveError(response: String): ErrorResponse? = try {
        // helper to fetch a tag or default to ""
        val tag: (String) -> String = { name -> getTag(response, name) ?: "" }

        // construct the error response
        ErrorResponse(
            responseOrigin   = tag("ResponseOrigin"),
            dsixReturnCode  = tag("DSIXReturnCode"),
            cmdStatus       = tag("CmdStatus"),
            textResponse    = tag("TextResponse"),
            sequenceNo      = tag("SequenceNo"),
            userTrace       = tag("UserTrace")
        ).also {
            Log.d(TAG, "Extracted ErrorResponse from XML")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error extracting error response: ${e.message}")
        // fall back to null
        null
    }

    /**
     * Extracts client version response data from XML.
     * 
     * This method parses XML response data and converts it into a ClientVersionResponse
     * object containing version information about the EMV client library.
     * This is useful for debugging and compatibility verification.
     * 
     * @param xml The XML response string from the DSI EMV library
     * @return A ClientVersionResponse object with extracted version data, or null if parsing fails
     */
    fun extractClientVersionResponse(xml: String): ClientVersionResponse? = try {
        // helper to fetch a tag or default to ""
        val tag: (String) -> String = { name -> getTag(xml, name) ?: "" }

        // construct the client version response
        ClientVersionResponse(
            // Basic response fields
            responseOrigin   = tag("ResponseOrigin"),
            dsixReturnCode  = tag("DSIXReturnCode"),
            cmdStatus       = tag("CmdStatus"),
            textResponse    = tag("TextResponse"),

            // Admin response fields
            tranCode        = tag("TranCode"),

            // Client version specific fields
            clientVersion   = getTag(xml, "ProductVersion"),
            clientLibraryName  = getTag(xml, "ProductName"),

            ).also {
            Log.d(TAG, "Successfully extracted ClientVersionResponse from XML")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error extracting client version response: ${e.message}")
        // fall back to null
        null
    }

    /**
     * Extracts card data response from XML.
     * 
     * This method parses XML response data and converts it into a CardData
     * object containing track information and status from a card reading operation.
     * It extracts track 1 and track 2 data along with their status indicators.
     * 
     * @param xml The XML response string from the DSI EMV library
     * @return A CardData object with extracted card information, or null if parsing fails
     */
    fun extractCardResponse(xml: String): CardData? = try {
        // helper to fetch a tag or default to ""
        val tag: (String) -> String = { name -> getTag(xml, name) ?: "" }

        // construct the client version response
        CardData(
            // Basic response fields
            responseOrigin  = tag("ResponseOrigin"),
            dsixReturnCode  = tag("DSIXReturnCode"),
            cmdStatus       = tag("CmdStatus"),
            textResponse    = tag("TextResponse"),
            track1Data      = tag("PrePaidTrack1"),
            track2Data      = tag("PrePaidTrack2"),
            track1Status    = tag("Track1Status"),
            track2Status    = tag("Track2Status")
            ).also {
            Log.d(TAG, "Successfully extracted ClientVersionResponse from XML")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error extracting client version response: ${e.message}")
        // fall back to null
        null
    }


}