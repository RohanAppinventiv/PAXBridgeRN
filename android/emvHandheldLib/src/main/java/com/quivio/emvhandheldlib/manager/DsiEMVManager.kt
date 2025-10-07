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

    private suspend fun resetPinPad() {
        posTransactionExecutor.resetPinPad()
    }

    private suspend fun downloadConfigParams() = withContext(Dispatchers.IO) {
        Log.d(PRINT_TAG, "Download Config Initiated: $currentPosState")
        posTransactionExecutor.downloadConfig()
    }

    suspend fun cancelTransaction(){
        posTransactionExecutor.cancelTransaction()
    }

    suspend fun runSaleTransaction(amount: String) = withContext(Dispatchers.IO) {
        posTransactionExecutor.doSale(amount)
    }

    suspend fun runRecurringTransaction(amount: String) = withContext(Dispatchers.IO) {
        posTransactionExecutor.doRecurringSale(amount)
    }

    suspend fun replaceCardInRecurring() = withContext(Dispatchers.IO) {
        posTransactionExecutor.doReplaceCardInRecurring()
    }

    suspend fun getClientVersion() = withContext(Dispatchers.IO) {
        posTransactionExecutor.getClientVersion()
    }

    suspend fun readPrepaidCard() = withContext(Dispatchers.IO) {
        posTransactionExecutor.readPrepaidCard()
    }

    fun registerListener(
        communicator: EMVTransactionCommunicator,
        configurationCommunicator: ConfigurationCommunicator? = null
    ) {
        this.communicator = communicator
        this.configCommunicator = configurationCommunicator
        posTransactionExecutor.addPosTransactionListener(processListener)
    }

    fun clearTransactionListener() {
        this.communicator = null
        this.configCommunicator = null
        posTransactionExecutor.clearAllListeners()
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
                }
            }
            runTransaction(currentTransaction = Operation.Reset, nextOperation = Operation.NONE)
        }
    }

    suspend fun runTransaction(currentTransaction: Operation = Operation.Reset, nextOperation: Operation, amount: String? = null){
        if(amount != null)
            transactionAmount = amount

        when(currentTransaction){
            Operation.NONE -> {
                transactionAmount = null
                currentPosState = PosState.IDLE
            }
            Operation.Reset -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                resetPinPad()
            }
            Operation.DownloadConfig -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                downloadConfigParams()
            }
            Operation.EMVSale -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                runSaleTransaction(transactionAmount!!)
            }
            Operation.RecurringSale -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                runRecurringTransaction(transactionAmount!!)
            }
            Operation.ReplaceCard -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                replaceCardInRecurring()
            }
            Operation.GetClientVersion -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                getClientVersion()
            }
            Operation.ReadPrepaidCard -> {
                currentPosState = PosState.Running(currentTransaction, nextOperation)
                readPrepaidCard()
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

}
