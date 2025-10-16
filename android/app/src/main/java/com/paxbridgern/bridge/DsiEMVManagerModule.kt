package com.paxbridgern.bridge

import android.util.Log
import com.facebook.react.bridge.*
import com.quivio.emvhandheldlib.manager.DsiEMVManager
import com.quivio.emvhandheldlib.contract.EMVTransactionCommunicator
import com.quivio.emvhandheldlib.contract.ConfigurationCommunicator
import com.quivio.emvhandheldlib.models.CardData
import com.quivio.emvhandheldlib.models.SaleTransactionResponse
import com.quivio.emvhandheldlib.models.ErrorResponse
import com.quivio.emvhandheldlib.models.RecurringTransactionResponse
import com.quivio.emvhandheldlib.models.ClientVersionResponse
import com.quivio.emvhandheldlib.models.ZeroAuthTransactionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.paxbridgern.BridgeEvent
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * React Native bridge module for DSI EMV payment processing functionality.
 * 
 * This module provides a bridge between React Native and the DSI EMV handheld library,
 * enabling payment processing operations including sales, recurring transactions,
 * card reading, and configuration management. It handles all communication between
 * the JavaScript layer and the native Android EMV library.
 * 
 * Key Features:
 * - Payment transaction processing (sale, recurring sale)
 * - Card reading and validation
 * - Configuration management and download
 * - Error handling and event emission
 * - Asynchronous operation support using coroutines
 * 
 * The module uses two main communicator interfaces:
 * - EMVTransactionCommunicator: Handles transaction-related callbacks
 * - ConfigurationCommunicator: Handles configuration-related callbacks
 * 
 * All operations are performed asynchronously and results are communicated back
 * to React Native through events and promises.
 * 
 * @param reactContext The React Native application context
 */
class DsiEMVManagerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    /** The DSI EMV Manager instance for handling payment operations */
    private var dsiEMVManager: DsiEMVManager? = null
    
    /** Coroutine scope for handling asynchronous operations on the main dispatcher */
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    /**
     * Returns the name of this native module as it will be exposed to React Native.
     * 
     * @return The module name "DsiEMVManager"
     */
    override fun getName(): String = "DsiEMVManager"

    /**
     * EMV Transaction Communicator implementation for handling transaction-related callbacks.
     * 
     * This object implements the EMVTransactionCommunicator interface and handles all
     * transaction-related events from the DSI EMV library. It processes various types
     * of responses including sales, recurring transactions, card reading, and error states.
     * 
     * All callbacks convert the native library responses into React Native-compatible
     * data structures and emit events to the JavaScript layer.
     */
    val emvCommunicator = object : EMVTransactionCommunicator {
        /**
         * Handles error responses from EMV operations.
         * 
         * This callback is triggered when any EMV operation encounters an error.
         * It extracts error details and sends them to React Native as an error event.
         * 
         * @param error The error response containing error details
         */
        override fun onError(error: ErrorResponse) {
            Log.e("DsiEMVManagerModule", "EMV Error: $error")
            val map = Arguments.createMap().apply {
                putString("responseOrigin", error.responseOrigin)
                putString("dsixReturnCode", error.dsixReturnCode)
                putString("cmdStatus", error.cmdStatus)
                putString("textResponse", error.textResponse)
                putString("sequenceNo", error.sequenceNo)
                putString("userTrace", error.userTrace)
            }
            sendEvent(BridgeEvent.ERROR_EVENT, map)
        }

        /**
         * Handles successful card reading operations.
         * 
         * This callback is triggered when a card is successfully read from the EMV device.
         * It extracts card data including track information and sends it to React Native.
         * 
         * @param cardData The card data containing track information and status
         */
        override fun onCardReadSuccessfully(cardData: CardData) {
            Log.d("DsiEMVManagerModule", "Card read successfully: $cardData")
            val map = Arguments.createMap().apply {
                putString("responseOrigin", cardData.responseOrigin)
                putString("dsixReturnCode", cardData.dsixReturnCode)
                putString("cmdStatus", cardData.cmdStatus)
                putString("textResponse", cardData.textResponse)
                putString("track1Status", cardData.track1Status)
                putString("track2Status", cardData.track2Status)
                if (cardData.track1Data != null) putString("track1Data", cardData.track1Data)
                if (cardData.track2Data != null) putString("track2Data", cardData.track2Data)
            }
            sendEvent(BridgeEvent.PREPAID_READ_SUCCESS, map)
        }

        /**
         * Handles successful sale transaction completion.
         * 
         * This callback is triggered when a sale transaction is successfully completed.
         * It extracts comprehensive transaction details including payment information,
         * cardholder data, and transaction metadata, then sends it to React Native.
         * 
         * @param saleDetails The sale transaction response containing all transaction details
         */
        override fun onSaleTransactionCompleted(saleDetails: SaleTransactionResponse) {
            Log.d("DsiEMVManagerModule", "Sale completed: $saleDetails")
            val map = Arguments.createMap().apply {
                // CmdResponse section
                putString("responseOrigin", saleDetails.responseOrigin)
                putString("dsixReturnCode", saleDetails.dsixReturnCode)
                putString("cmdStatus", saleDetails.cmdStatus)
                putString("textResponse", saleDetails.textResponse)

                // TranResponse section
                putString("merchantID", saleDetails.merchantID)
                putString("payAPIId", saleDetails.payAPIId)
                putString("acctNo", saleDetails.acctNo)
                putString("cardType", saleDetails.cardType)
                putString("tranCode", saleDetails.tranCode)
                putString("authCode", saleDetails.authCode)
                putString("avsResult", saleDetails.avsResult)
                putString("cvvResult", saleDetails.cvvResult)
                putString("captureStatus", saleDetails.captureStatus)
                putString("refNo", saleDetails.refNo)

                // Amount fields
                putMap("amount", Arguments.createMap().apply {
                    putString("purchase", saleDetails.amount.purchase)
                    putString("authorize", saleDetails.amount.authorize)
                })

                putString("cardholderName", saleDetails.cardholderName)
                putString("acqRefData", saleDetails.acqRefData)
                putString("processorToken", saleDetails.processorToken)
                putString("processData", saleDetails.processData)
                putString("recordNo", saleDetails.recordNo)
                putString("recurringData", saleDetails.recurringData)
                putString("entryMethod", saleDetails.entryMethod)
                putString("date", saleDetails.date)
                putString("time", saleDetails.time)
                putString("cvm", saleDetails.cvm)
            }
            sendEvent(BridgeEvent.SALE_SUCCESS, map)
        }

        /**
         * Handles successful recurring sale transaction completion.
         * 
         * This callback is triggered when a recurring sale transaction is successfully completed.
         * It processes the recurring transaction response and sends the details to React Native.
         * 
         * @param recurringDetails The recurring transaction response containing transaction details
         */
        override fun onRecurringSaleCompleted(recurringDetails: RecurringTransactionResponse) {
            Log.d("DsiEMVManagerModule", "Recurring sale completed: $recurringDetails")
            val map = Arguments.createMap().apply {
                // CmdResponse section
                putString("responseOrigin", recurringDetails.responseOrigin)
                putString("dsixReturnCode", recurringDetails.dsixReturnCode)
                putString("cmdStatus", recurringDetails.cmdStatus)
                putString("textResponse", recurringDetails.textResponse)

                // TranResponse section
                putString("merchantID", recurringDetails.merchantID)
                putString("payAPIId", recurringDetails.payAPIId)
                putString("acctNo", recurringDetails.acctNo)
                putString("cardType", recurringDetails.cardType)
                putString("tranCode", recurringDetails.tranCode)
                putString("authCode", recurringDetails.authCode)
                putString("avsResult", recurringDetails.avsResult)
                putString("cvvResult", recurringDetails.cvvResult)
                putString("captureStatus", recurringDetails.captureStatus)
                putString("refNo", recurringDetails.refNo)

                // Amount fields
                putMap("amount", Arguments.createMap().apply {
                    putString("purchase", recurringDetails.amount.purchase)
                    putString("authorize", recurringDetails.amount.authorize)
                })

                putString("cardholderName", recurringDetails.cardholderName)
                putString("acqRefData", recurringDetails.acqRefData)
                putString("processorToken", recurringDetails.processorToken)
                putString("processData", recurringDetails.processData)
                putString("recordNo", recurringDetails.recordNo)
                putString("recurringData", recurringDetails.recurringData)
                putString("entryMethod", recurringDetails.entryMethod)
                putString("date", recurringDetails.date)
                putString("time", recurringDetails.time)
                putString("cvm", recurringDetails.cvm)
            }
            sendEvent(BridgeEvent.RECURRING_SALE_SUCCESS, map)
        }

        /**
         * Handles successful card replacement transaction completion.
         * 
         * This callback is triggered when a card replacement (zero auth) transaction is completed.
         * It processes the zero auth response and sends comprehensive transaction details to React Native.
         * 
         * @param zeroAuthData The zero auth transaction response containing card replacement details
         */
        override fun onCardReplaceTransactionCompleted(zeroAuthData: ZeroAuthTransactionResponse) {
            Log.d("DsiEMVManagerModule", "Card replace completed: $zeroAuthData")
            val map = Arguments.createMap().apply {
                // Basic response fields
                putString("responseOrigin", zeroAuthData.responseOrigin)
                putString("dsixReturnCode", zeroAuthData.dsixReturnCode)
                putString("cmdStatus", zeroAuthData.cmdStatus)
                putString("textResponse", zeroAuthData.textResponse)
                putString("sequenceNo", zeroAuthData.sequenceNo)
                putString("userTrace", zeroAuthData.userTrace)

                // Transaction details
                putString("merchantID", zeroAuthData.merchantID)
                putString("acctNo", zeroAuthData.acctNo)
                putString("cardType", zeroAuthData.cardType)
                putString("tranCode", zeroAuthData.tranCode)
                putString("authCode", zeroAuthData.authCode)
                putString("refNo", zeroAuthData.refNo)
                putString("invoiceNo", zeroAuthData.invoiceNo)

                // Amount fields
                putMap("amount", Arguments.createMap().apply {
                    putString("purchase", zeroAuthData.amount.purchase)
                    putString("authorize", zeroAuthData.amount.authorize)
                })

                // Additional transaction data
                putString("acqRefData", zeroAuthData.acqRefData)
                putString("processData", zeroAuthData.processData)
                putString("cardHolderID", zeroAuthData.cardHolderID)
                putString("recordNo", zeroAuthData.recordNo)
                putString("cardholderName", zeroAuthData.cardholderName)
                putString("entryMethod", zeroAuthData.entryMethod)
                putString("date", zeroAuthData.date)
                putString("time", zeroAuthData.time)
                putString("applicationLabel", zeroAuthData.applicationLabel)

                // EMV specific fields
                putString("aid", zeroAuthData.aid)
                putString("tvr", zeroAuthData.tvr)
                putString("iad", zeroAuthData.iad)
                putString("tsi", zeroAuthData.tsi)
                putString("arc", zeroAuthData.arc)
                putString("cvm", zeroAuthData.cvm)
                putString("payAPIId", zeroAuthData.payAPIId)
            }
            sendEvent(BridgeEvent.ZERO_AUTH_SUCCESS, map)
        }

        /**
         * Handles successful client version retrieval.
         * 
         * This callback is triggered when the client version information is successfully retrieved.
         * It sends version details to React Native for debugging and compatibility purposes.
         * 
         * @param clientVersionDetails The client version response containing version information
         */
        override fun onClientVersionCompleted(clientVersionDetails: ClientVersionResponse) {
            Log.d("DsiEMVManagerModule", "Client version: $clientVersionDetails")
            val map = Arguments.createMap().apply {
                // Basic response fields
                putString("responseOrigin", clientVersionDetails.responseOrigin)
                putString("dsixReturnCode", clientVersionDetails.dsixReturnCode)
                putString("cmdStatus", clientVersionDetails.cmdStatus)
                putString("textResponse", clientVersionDetails.textResponse)

                // Admin response fields
                putString("tranCode", clientVersionDetails.tranCode)

                // Client version specific fields
                putString("clientVersion", clientVersionDetails.clientVersion)
                putString("clientLibraryName", clientVersionDetails.clientLibraryName)
            }
            sendEvent(BridgeEvent.CLIENT_VERSION_FETCH_SUCCESS, map)
        }

        /**
         * Handles display messages from the EMV library.
         * 
         * This callback is triggered when the EMV library needs to display a message to the user.
         * It forwards the message to React Native for display in the UI.
         * 
         * @param message The message to display to the user
         */
        override fun onShowMessage(message: String) {
            Log.d("DsiEMVManagerModule", "Message: $message")
            sendEvent(BridgeEvent.MESSAGE_EVENT, message)
        }
    }

    /**
     * Configuration Communicator implementation for handling configuration-related callbacks.
     * 
     * This object implements the ConfigurationCommunicator interface and handles all
     * configuration-related events from the DSI EMV library. It processes configuration
     * operations including setup, ping tests, and error states.
     * 
     * All callbacks convert the native library responses into React Native-compatible
     * data structures and emit events to the JavaScript layer.
     */
    val configCommunicator = object : ConfigurationCommunicator {
        /**
         * Handles configuration errors.
         * 
         * This callback is triggered when a configuration operation encounters an error.
         * It sends the error message to React Native for handling.
         * 
         * @param errorMessage The error message describing the configuration issue
         */
        override fun onConfigError(errorMessage: String) {
            Log.e("DsiEMVManagerModule", "Config error: $errorMessage")
            sendEvent(BridgeEvent.CONFIG_ERROR, errorMessage)
        }

        /**
         * Handles configuration ping failure.
         * 
         * This callback is triggered when the configuration ping test fails.
         * It sends a failure status to React Native with timestamp information.
         */
        override fun onConfigPingFailed() {
            Log.e("DsiEMVManagerModule", "Config ping failed")
            val failedMap = Arguments.createMap().apply {
                putString("status", "failed")
                putString("message", "Configuration ping failed")
                putString("timestamp", System.currentTimeMillis().toString())
            }
            sendEvent(BridgeEvent.CONFIG_PING_FAIL, failedMap)
        }

        /**
         * Handles successful configuration ping.
         * 
         * This callback is triggered when the configuration ping test succeeds.
         * It sends a success status to React Native with timestamp information.
         */
        override fun onConfigPingSuccess() {
            Log.d("DsiEMVManagerModule", "Config ping success")
            val successMap = Arguments.createMap().apply {
                putString("status", "success")
                putString("message", "Configuration ping successful")
                putString("timestamp", System.currentTimeMillis().toString())
            }
            sendEvent(BridgeEvent.CONFIG_PING_SUCCESS, successMap)
        }

        /**
         * Handles configuration completion.
         * 
         * This callback is triggered when the configuration setup is fully completed.
         * It sends a completion status to React Native with timestamp information.
         */
        override fun onConfigCompleted() {
            Log.d("DsiEMVManagerModule", "Config completed")
            val completedMap = Arguments.createMap().apply {
                putString("status", "completed")
                putString("message", "Configuration setup completed")
                putString("timestamp", System.currentTimeMillis().toString())
            }
            sendEvent(BridgeEvent.CONFIG_COMPLETED, completedMap)
        }
    }
    
    /**
     * Initializes the DSI EMV Manager with the provided configuration.
     * 
     * This method sets up the EMV manager with merchant and device configuration,
     * registers the communicator listeners, and prepares the system for payment operations.
     * 
     * @param config A ReadableMap containing the POS configuration parameters:
     *               - merchantID: String (required)
     *               - onlineMerchantID: String (required)
     *               - isSandBox: Boolean (optional, defaults to true)
     *               - secureDeviceName: String (required)
     *               - operatorID: String (required)
     *               - posPackageID: String (required)
     *               - pinPadIPAddress: String (required)
     *               - pinPadPort: String (required)
     * @param promise A Promise that resolves with success message or rejects with error
     */
    @ReactMethod
    fun initialize(config: ReadableMap, promise: Promise) {
        try {
            Log.d("DsiEMVManagerModule", "Initializing with config: ${config.toHashMap()}")
            val configFactory = POSConfigFactory.processMap(config)
            dsiEMVManager = DsiEMVManager(reactApplicationContext, configFactory)
            dsiEMVManager!!.registerListener(emvCommunicator, configCommunicator)
            promise.resolve("Initialized successfully")
            
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Initialization failed", e)
            promise.reject("INIT_ERROR", e.message, e)
        }
    }
    
    /**
     * Initiates a sale transaction with the specified amount.
     * 
     * This method starts a payment transaction process asynchronously. The actual
     * transaction result will be delivered through the EMV communicator callbacks.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     * @param promise A Promise that resolves immediately or rejects if setup fails
     */
    @ReactMethod
    fun doSale(amount: String, promise: Promise) {
        try {
            val manager = dsiEMVManager ?: throw IllegalStateException("Manager not initialized")
            coroutineScope.launch {
                try {
                    manager.runSaleTransaction(amount)
                } catch (e: Exception) {
                    Log.e("DsiEMVManagerModule", "Sale failed", e)
                    promise.reject("SALE_ERROR", e.message, e)
                }
            }
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Sale setup failed", e)
            promise.reject("SALE_SETUP_ERROR", e.message, e)
        }
    }
    
    /**
     * Initiates a recurring sale transaction with the specified amount.
     * 
     * This method starts a recurring payment transaction process asynchronously.
     * The actual transaction result will be delivered through the EMV communicator callbacks.
     * 
     * @param amount The transaction amount as a string (e.g., "10.00")
     * @param promise A Promise that resolves immediately or rejects if setup fails
     */
    @ReactMethod
    fun doRecurringSale(amount: String, promise: Promise) {
        try {
            val manager = dsiEMVManager ?: throw IllegalStateException("Manager not initialized")
            
            coroutineScope.launch {
                try {
                    manager.runRecurringTransaction(amount)
                } catch (e: Exception) {
                    Log.e("DsiEMVManagerModule", "Recurring sale failed", e)
                    promise.reject("RECURRING_SALE_ERROR", e.message, e)
                }
            }
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Recurring sale setup failed", e)
            promise.reject("RECURRING_SALE_SETUP_ERROR", e.message, e)
        }
    }
    
    /**
     * Cancels the currently active transaction.
     * 
     * This method cancels any ongoing transaction operation. It should be called
     * when the user wants to abort a transaction in progress.
     * 
     * @param promise A Promise that resolves when cancellation is complete or rejects on error
     */
    @ReactMethod
    fun cancelTransaction(promise: Promise) {
        try {
            val manager = dsiEMVManager ?: throw IllegalStateException("Manager not initialized")
            
            coroutineScope.launch {
                try {
                    manager.cancelTransaction()
                } catch (e: Exception) {
                    Log.e("DsiEMVManagerModule", "Cancel failed", e)
                    promise.reject("CANCEL_ERROR", e.message, e)
                }
            }
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Cancel setup failed", e)
            promise.reject("CANCEL_SETUP_ERROR", e.message, e)
        }
    }
    
    /**
     * Downloads configuration parameters from the payment processor.
     * 
     * This method initiates the download of configuration parameters required
     * for payment processing. The result will be delivered through configuration
     * communicator callbacks.
     * 
     * @param promise A Promise that resolves immediately or rejects if setup fails
     */
    @ReactMethod
    fun downloadConfig(promise: Promise) {
        try {
            val manager = dsiEMVManager ?: throw IllegalStateException("Manager not initialized")
            
            coroutineScope.launch {
                try {
                    manager.downloadConfigParams()
                } catch (e: Exception) {
                    Log.e("DsiEMVManagerModule", "Config download failed", e)
                    promise.reject("CONFIG_DOWNLOAD_ERROR", e.message, e)
                }
            }
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Config download setup failed", e)
            promise.reject("CONFIG_DOWNLOAD_SETUP_ERROR", e.message, e)
        }
    }
    
    /**
     * Retrieves the client version information.
     * 
     * This method fetches version information about the EMV client library.
     * The result will be delivered through the EMV communicator callbacks.
     * 
     * @param promise A Promise that resolves immediately or rejects if setup fails
     */
    @ReactMethod
    fun getClientVersion(promise: Promise) {
        try {
            val manager = dsiEMVManager ?: throw IllegalStateException("Manager not initialized")
            
            coroutineScope.launch {
                try {
                    manager.getClientVersion()
                } catch (e: Exception) {
                    Log.e("DsiEMVManagerModule", "Client version failed", e)
                    promise.reject("CLIENT_VERSION_ERROR", e.message, e)
                }
            }
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Client version setup failed", e)
            promise.reject("CLIENT_VERSION_SETUP_ERROR", e.message, e)
        }
    }
    
    /**
     * Initiates reading of a prepaid card.
     * 
     * This method starts the process of reading card data from a prepaid card.
     * The card data will be delivered through the EMV communicator callbacks.
     * 
     * @param promise A Promise that resolves immediately or rejects if setup fails
     */
    @ReactMethod
    fun readPrepaidCard(promise: Promise) {
        try {
            val manager = dsiEMVManager ?: throw IllegalStateException("Manager not initialized")
            
            coroutineScope.launch {
                try {
                    manager.readPrepaidCard()
                } catch (e: Exception) {
                    Log.e("DsiEMVManagerModule", "Prepaid card read failed", e)
                    promise.reject("PREPAID_READ_ERROR", e.message, e)
                }
            }
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Prepaid card read setup failed", e)
            promise.reject("PREPAID_READ_SETUP_ERROR", e.message, e)
        }
    }


    /**
     * Initiates a credit card replacement transaction.
     * 
     * This method starts a card replacement (zero auth) transaction process.
     * The transaction result will be delivered through the EMV communicator callbacks.
     * 
     * @param promise A Promise that resolves immediately or rejects if setup fails
     */
    @ReactMethod
    fun replaceCreditCard(promise: Promise) {
        try {
            val manager = dsiEMVManager ?: throw IllegalStateException("Manager not initialized")
            coroutineScope.launch {
                try {
                    manager.replaceCardInRecurring()
                } catch (e: Exception) {
                    Log.e("DsiEMVManagerModule", "Replace credit card", e)
                    promise.reject("ERROR", e.message, e)
                }
            }
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Prepaid card read setup failed", e)
            promise.reject("ERROR", e.message, e)
        }
    }

    /**
     * Cleans up resources and unregisters listeners.
     * 
     * This method should be called when the module is no longer needed to properly
     * clean up resources, unregister listeners, and prevent memory leaks.
     * 
     * @param promise A Promise that resolves when cleanup is complete or rejects on error
     */
    @ReactMethod
    fun cleanup(promise: Promise) {
        try {
            dsiEMVManager?.clearTransactionListener()
            dsiEMVManager = null
            promise.resolve("Cleanup completed")
        } catch (e: Exception) {
            Log.e("DsiEMVManagerModule", "Cleanup failed", e)
            promise.reject("CLEANUP_ERROR", e.message, e)
        }
    }

    /**
     * Sends an event to the React Native JavaScript layer.
     * 
     * This private helper method is used to emit events from the native Android layer
     * to the React Native JavaScript layer. It handles the conversion of native data
     * structures to React Native-compatible formats and ensures proper event delivery.
     * 
     * @param eventName The name of the event to emit (should match BridgeEvent constants)
     * @param params The event parameters (can be null, String, or WritableMap)
     */
    private fun sendEvent(eventName: String, params: Any?) {
        Log.d("DsiEMVManagerModule", "sendEvent: $eventName with params: $params")
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
}
