package com.quivio.emvhandheldlib.manager

import android.content.Context
import android.util.Log
import com.datacap.android.ProcessTransactionResponseListener
import com.quivio.emvhandheldlib.PRINT_TAG
import com.quivio.emvhandheldlib.contract.ConfigFactory
import com.quivio.emvhandheldlib.contract.ConfigurationCommunicator
import com.quivio.emvhandheldlib.contract.EMVTransactionCommunicator
import com.quivio.emvhandheldlib.executor.POSTransactionExecutor
import com.quivio.emvhandheldlib.extractor.XMLResponseExtractor
import com.quivio.emvhandheldlib.models.CardData
import com.quivio.emvhandheldlib.models.ErrorCode
import com.quivio.emvhandheldlib.models.Operation
import com.quivio.emvhandheldlib.models.PosState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

/**
 * Main manager class for DSI EMV payment processing operations.
 * 
 * This class serves as the central coordinator for all EMV payment operations.
 * It manages the state machine, coordinates between different components,
 * and handles the flow of transactions from initiation to completion.
 * 
 * Key Responsibilities:
 * - State management for POS operations
 * - Transaction flow coordination
 * - Response processing and routing
 * - Error handling and recovery
 * - Listener management for callbacks
 * 
 * The manager uses a state machine pattern to ensure proper sequencing
 * of operations and handles both success and error scenarios gracefully.
 * 
 * @param context The Android application context
 * @param posConfig The POS configuration factory containing merchant and device settings
 */
class DsiEMVManager(
    val context: Context,
    posConfig: ConfigFactory
) {
    private var currentPosState: PosState = PosState.IDLE
    private var communicator: EMVTransactionCommunicator? = null
    private var configCommunicator: ConfigurationCommunicator? = null
    private var transactionAmount: String? = null
    private val posTransactionExecutor by lazy {
        POSTransactionExecutor(context, posConfig)
    }
    private val posXMLResponseExtractor by lazy {
        XMLResponseExtractor()
    }
    private val processListener = ProcessTransactionResponseListener { res ->
        Log.d(PRINT_TAG, "Process Response: $res")
        CoroutineScope(Dispatchers.IO).launch {
            // First check if a process is already running
            if (posXMLResponseExtractor.isProcessAlreadyRunning(res)) {
                Log.d(PRINT_TAG, "Process already running detected. Auto-cancelling transaction...")
                try {
                    cancelTransaction()
                    Log.d(PRINT_TAG, "Auto-cancel transaction called successfully")
                } catch (e: Exception) {
                    Log.e(PRINT_TAG, "Auto-cancel failed: ${e.message}")
                }
                return@launch // Return early after cancel
            }

            // Call isFailed function and store result
            val resStatus = posXMLResponseExtractor.isFailed(res)

            // Check if error or success
            if (resStatus) {
                // Error case
                checkErrorResponse(res)
            } else {
                // Success case
                checkSuccessResponse(res)
            }
        }
    }

    /**
     * Downloads configuration parameters from the payment processor.
     * 
     * This method initiates the download of configuration parameters required
     * for payment processing. It runs on the IO dispatcher to avoid blocking
     * the main thread.
     */
    suspend fun downloadConfigParams() = withContext(Dispatchers.IO) {
        runTransaction(
            currentTransaction = Operation.DownloadConfig,
            nextOperation = Operation.NONE
        )
    }

    /**
     * Executes a standard EMV sale transaction.
     * 
     * This method processes a payment transaction with the specified amount.
     * It runs on the IO dispatcher and includes a reset operation after completion.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     */
    suspend fun runSaleTransaction(amount: String) = withContext(Dispatchers.IO) {
       runTransaction(
           currentTransaction = Operation.EMVSale,
           nextOperation = Operation.Reset,
           amount = amount
       )
    }

    /**
     * Executes a recurring EMV sale transaction.
     * 
     * This method processes a recurring payment transaction with the specified amount.
     * It runs on the IO dispatcher and includes a reset operation after completion.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     */
    suspend fun runRecurringTransaction(amount: String) = withContext(Dispatchers.IO) {
       runTransaction(
           currentTransaction = Operation.RecurringSale,
           nextOperation = Operation.Reset,
           amount = amount
       )
    }

    /**
     * Executes a card replacement transaction for recurring payments.
     * 
     * This method processes a card replacement (zero auth) transaction to update
     * the payment method for recurring payments. It runs on the IO dispatcher
     * and includes a reset operation after completion.
     */
    suspend fun replaceCardInRecurring() = withContext(Dispatchers.IO) {
        runTransaction(
            currentTransaction = Operation.ReplaceCard,
            nextOperation = Operation.Reset
        )
    }

    /**
     * Retrieves the client version information.
     * 
     * This method fetches version information about the EMV client library.
     * It runs on the IO dispatcher and includes a reset operation after completion.
     * This is useful for debugging and compatibility checks.
     */
    suspend fun getClientVersion() = withContext(Dispatchers.IO) {
        runTransaction(
            currentTransaction = Operation.GetClientVersion,
            nextOperation = Operation.Reset
        )
    }

    /**
     * Reads data from a prepaid card.
     * 
     * This method initiates the reading of card data from a prepaid card.
     * It runs on the IO dispatcher and includes a reset operation after completion.
     * The card data will be returned through the communicator callbacks.
     */
    suspend fun readPrepaidCard() = withContext(Dispatchers.IO) {
        runTransaction(
            currentTransaction = Operation.ReadPrepaidCard,
            nextOperation = Operation.Reset
        )
    }

    /**
     * Cancels the currently active transaction.
     * 
     * This method cancels any ongoing transaction operation. It should be called
     * when the user wants to abort a transaction in progress or when an error
     * condition requires transaction cancellation.
     */
    suspend fun cancelTransaction(){
        posTransactionExecutor.cancelTransaction()
    }

    private suspend fun runTransaction(
        currentTransaction: Operation = Operation.Reset,
        nextOperation: Operation,
        amount: String? = null
    ){
        if(amount != null)
            transactionAmount = amount

        when(currentTransaction){
            Operation.NONE -> {
                transactionAmount = null
                currentPosState = PosState.IDLE
            }
            Operation.Reset -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                posTransactionExecutor.resetPinPad()
            }
            Operation.DownloadConfig -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                posTransactionExecutor.downloadConfig()
            }
            Operation.EMVSale -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                amount?.let { amt -> posTransactionExecutor.doSale(amt) }
            }
            Operation.RecurringSale -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                amount?.let { amt -> posTransactionExecutor.doSale(amt) }
            }
            Operation.ReplaceCard -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                posTransactionExecutor.doReplaceCardInRecurring()
            }
            Operation.GetClientVersion -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                posTransactionExecutor.getClientVersion()
            }
            Operation.ReadPrepaidCard -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                posTransactionExecutor.readPrepaidCard()
            }
        }
    }

    suspend fun checkSuccessResponse(xml: String) {
        when (currentPosState.currentOperation) {
            Operation.DownloadConfig -> {
                configCommunicator?.onConfigPingSuccess()
                runTransaction(Operation.NONE, Operation.NONE)
            }

            Operation.EMVSale -> {
                val response = posXMLResponseExtractor.extractSaleResponse(xml)
                response?.let {
                    communicator?.onSaleTransactionCompleted(it)
                }
                runTransaction(Operation.Reset, Operation.NONE)
            }

            Operation.RecurringSale -> {
                val recurringDetails = posXMLResponseExtractor.extractRecurringTransactionResponse(xml)
                recurringDetails?.let {
                    communicator?.onRecurringSaleCompleted(it)
                }
                runTransaction(Operation.Reset, Operation.NONE)
            }
            Operation.ReplaceCard -> {
                val recurringDetails = posXMLResponseExtractor.extractZeroAuthResponse(xml)
                recurringDetails?.let {
                    communicator?.onCardReplaceTransactionCompleted(it)
                }
                runTransaction(Operation.Reset, Operation.NONE)
            }
            Operation.GetClientVersion -> {
                val clientVersionDetails = posXMLResponseExtractor.extractClientVersionResponse(xml)
                clientVersionDetails?.let {
                    communicator?.onClientVersionCompleted(it)
                }
                runTransaction(Operation.NONE, Operation.NONE)
            }
            Operation.ReadPrepaidCard -> {
                val cardData = posXMLResponseExtractor.extractCardResponse(xml)
                cardData?.let {data ->
                    communicator?.onCardReadSuccessfully(data)
                }
                runTransaction(Operation.Reset, Operation.NONE)
            }

            Operation.Reset -> {
                runTransaction(currentPosState.nextOperation, Operation.NONE)
            }

            Operation.NONE -> {
                runTransaction(Operation.NONE, Operation.NONE)
            }
        }
    }

    private suspend fun checkErrorResponse(xml: String) {
        val error = posXMLResponseExtractor.resolveError(xml)
        error?.let { errorRes ->
            when (currentPosState.currentOperation) {
                Operation.Reset -> {
                    if (errorRes.dsixReturnCode == ErrorCode.PSCS_ERROR.code) {
                        runTransaction(currentTransaction = Operation.DownloadConfig, nextOperation = currentPosState.nextOperation)
                        return
                    } else {
                        configCommunicator?.onConfigError(errorRes.textResponse)
                    }
                }
                else -> {
                    communicator?.onError(error)
                    runTransaction(currentTransaction = Operation.Reset, nextOperation = Operation.NONE)
                }
            }
        }
    }

    /**
     * Registers listeners for transaction and configuration events.
     * 
     * This method sets up the communication channels between the manager and
     * the external components that need to respond to EMV events. It registers
     * both transaction and configuration communicators.
     * 
     * @param communicator The EMV transaction communicator for handling transaction events
     * @param configurationCommunicator The configuration communicator for handling config events (optional)
     */
    fun registerListener(
        communicator: EMVTransactionCommunicator,
        configurationCommunicator: ConfigurationCommunicator? = null
    ) {
        this.communicator = communicator
        this.configCommunicator = configurationCommunicator
        posTransactionExecutor.addPosTransactionListener(processListener)
    }

    /**
     * Clears all registered listeners and cleans up resources.
     * 
     * This method removes all registered communicators and clears all listeners
     * from the transaction executor. It should be called when the manager is
     * no longer needed to prevent memory leaks and ensure proper cleanup.
     */
    fun clearTransactionListener() {
        this.communicator = null
        this.configCommunicator = null
        posTransactionExecutor.clearAllListeners()
    }

}
