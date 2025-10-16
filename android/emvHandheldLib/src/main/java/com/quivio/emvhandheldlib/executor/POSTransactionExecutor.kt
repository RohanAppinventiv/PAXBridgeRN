package com.quivio.emvhandheldlib.executor

import android.content.Context
import android.util.Log
import com.datacap.android.ProcessTransactionResponseListener
import com.quivio.emvhandheldlib.PRINT_TAG
import com.quivio.emvhandheldlib.builder.DsiEMVInstanceBuilder
import com.quivio.emvhandheldlib.builder.DsiEMVRequestBuilder
import com.quivio.emvhandheldlib.contract.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.getValue

/**
 * Executor class for handling POS (Point of Sale) transaction operations.
 * 
 * This class serves as the main executor for all EMV payment operations. It coordinates
 * between the DSI EMV Android library, request builders, and response processing.
 * All operations are performed asynchronously using coroutines to ensure non-blocking
 * execution and proper UI responsiveness.
 * 
 * Key Responsibilities:
 * - Execute various EMV transaction types (sale, recurring, card reading, etc.)
 * - Manage communication with the DSI EMV Android library
 * - Handle transaction cancellation and cleanup
 * - Provide listener management for transaction responses
 * 
 * The executor uses lazy initialization for its dependencies to ensure they are
 * created only when needed and to avoid potential initialization issues.
 * 
 * @param context The Android application context
 * @param posConfig The POS configuration factory containing merchant and device settings
 */
class POSTransactionExecutor(context: Context, posConfig: ConfigFactory) {

    /** Lazy-initialized DSI EMV Android library instance */
    private val dsiEMVAndroidLib  by lazy {
        DsiEMVInstanceBuilder.getInstance(context)
    }
    
    /** Lazy-initialized request builder for creating XML requests */
    private val requestBuilder by lazy {
        DsiEMVRequestBuilder(posConfig)
    }

    /**
     * Executes a standard EMV sale transaction.
     * 
     * This method processes a payment transaction with the specified amount.
     * It builds the appropriate XML request and sends it to the DSI EMV library
     * for processing. The operation runs on the IO dispatcher to avoid blocking
     * the main thread.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     */
    suspend fun doSale(amount: String) {
        withContext(Dispatchers.IO){
            val request = requestBuilder.buildEMVSaleRequest(amount)
            Log.d(PRINT_TAG, "Sale request prepared: $request")
            dsiEMVAndroidLib.ProcessTransaction(request)
        }
    }

    /**
     * Executes a recurring EMV sale transaction.
     * 
     * This method processes a recurring payment transaction with the specified amount.
     * It builds the appropriate XML request for recurring payments and sends it to
     * the DSI EMV library for processing.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     */
    suspend fun doRecurringSale(amount: String) {
        withContext(Dispatchers.IO){
            val request = requestBuilder.buildEMVRecurringSaleRequest(amount)
            Log.d(PRINT_TAG, "Recurring Sale request prepared: $request")
            dsiEMVAndroidLib.ProcessTransaction(request)
        }
    }

    /**
     * Executes a card replacement transaction for recurring payments.
     * 
     * This method processes a card replacement (zero auth) transaction to update
     * the payment method for recurring payments. It performs a zero-amount
     * authorization to validate the new card.
     */
    suspend fun doReplaceCardInRecurring() {
        withContext(Dispatchers.IO){
            val request = requestBuilder.buildReplaceCardInRecurringRequest()
            Log.d(PRINT_TAG, "Replace Card in Recurring request prepared: $request")
            dsiEMVAndroidLib.ProcessTransaction(request)
        }
    }

    /**
     * Cancels the currently active transaction.
     * 
     * This method cancels any ongoing transaction operation. It should be called
     * when the user wants to abort a transaction in progress or when an error
     * condition requires transaction cancellation.
     */
    suspend fun cancelTransaction(){
        dsiEMVAndroidLib.CancelRequest()
    }

    /**
     * Downloads configuration parameters from the payment processor.
     * 
     * This method initiates the download of configuration parameters required
     * for payment processing. It builds the appropriate XML request and sends
     * it to the DSI EMV library for processing.
     */
    suspend fun downloadConfig(){
        withContext(Dispatchers.IO){
            Log.d(PRINT_TAG, "Inside downloadConfig()")
            val request = requestBuilder.buildEMVParamDownloadRequest()
            Log.d(PRINT_TAG, "Prepared DownloadParam Request: $request")
            dsiEMVAndroidLib.ProcessTransaction(
                request
            )
        }
    }

    /**
     * Retrieves the client version information.
     * 
     * This method fetches version information about the EMV client library.
     * It builds the appropriate XML request and sends it to the DSI EMV library
     * for processing. This is useful for debugging and compatibility checks.
     */
    suspend fun getClientVersion(){
        withContext(Dispatchers.IO){
            Log.d(PRINT_TAG, "Inside getClientVersion()")
            val request = requestBuilder.buildClientVersionRequest()
            Log.d(PRINT_TAG, "Prepared ClientVersion Request: $request")
            dsiEMVAndroidLib.ProcessTransaction(
                request
            )
        }
    }

    /**
     * Resets the PIN pad device.
     * 
     * This method resets the PIN pad device to clear any pending operations
     * and prepare it for new transactions. It builds the appropriate XML request
     * and sends it to the DSI EMV library for processing.
     */
    suspend fun resetPinPad(){
        withContext(Dispatchers.IO){
            Log.d(PRINT_TAG, "Inside Reset Pin()")
            val request = requestBuilder.buildPinPadResetRequest()
            Log.d(PRINT_TAG, "Reset Request R: $request")
            dsiEMVAndroidLib.ProcessTransaction(
                request
            )
        }
    }

    /**
     * Reads data from a prepaid card.
     * 
     * This method initiates the reading of card data from a prepaid card.
     * It builds the appropriate XML request and sends it to the DSI EMV library
     * for processing. The card data will be returned through the response listener.
     */
    suspend fun readPrepaidCard(){
        withContext(Dispatchers.IO){
            Log.d(PRINT_TAG, "Inside readPrepaidCard()")
            dsiEMVAndroidLib.ProcessTransaction(
                requestBuilder.buildCollectCardDataRequest()
            )
        }
    }

    /**
     * Adds a listener for transaction response events.
     * 
     * This method registers a listener to receive transaction response events
     * from the DSI EMV library. The listener will be called when transaction
     * responses are received.
     * 
     * @param callback The listener to register for transaction responses
     */
    fun addPosTransactionListener(callback: ProcessTransactionResponseListener){
        dsiEMVAndroidLib.AddProcessTransactionResponseListener(callback)
    }

    /**
     * Clears all registered listeners.
     * 
     * This method removes all registered listeners for both transaction responses
     * and card data collection responses. It should be called when cleaning up
     * resources or when listeners are no longer needed.
     */
    fun clearAllListeners(){
        dsiEMVAndroidLib.ClearProcessTransactionResponseListeners()
        dsiEMVAndroidLib.ClearCollectCardDataResponseListeners()
    }

}