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

class DsiEMVManagerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    private var dsiEMVManager: DsiEMVManager? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun getName(): String = "DsiEMVManager"

    val emvCommunicator = object : EMVTransactionCommunicator {
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

        override fun onShowMessage(message: String) {
            Log.d("DsiEMVManagerModule", "Message: $message")
            sendEvent(BridgeEvent.MESSAGE_EVENT, message)
        }
    }

    val configCommunicator = object : ConfigurationCommunicator {
        override fun onConfigError(errorMessage: String) {
            Log.e("DsiEMVManagerModule", "Config error: $errorMessage")
            sendEvent(BridgeEvent.CONFIG_ERROR, errorMessage)
        }

        override fun onConfigPingFailed() {
            Log.e("DsiEMVManagerModule", "Config ping failed")
            val failedMap = Arguments.createMap().apply {
                putString("status", "failed")
                putString("message", "Configuration ping failed")
                putString("timestamp", System.currentTimeMillis().toString())
            }
            sendEvent(BridgeEvent.CONFIG_PING_FAIL, failedMap)
        }

        override fun onConfigPingSuccess() {
            Log.d("DsiEMVManagerModule", "Config ping success")
            val successMap = Arguments.createMap().apply {
                putString("status", "success")
                putString("message", "Configuration ping successful")
                putString("timestamp", System.currentTimeMillis().toString())
            }
            sendEvent(BridgeEvent.CONFIG_PING_SUCCESS, successMap)
        }

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

    private fun sendEvent(eventName: String, params: Any?) {
        Log.d("DsiEMVManagerModule", "sendEvent: $eventName with params: $params")
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
}
