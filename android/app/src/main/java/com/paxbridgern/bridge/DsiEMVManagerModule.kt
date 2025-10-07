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

class DsiEMVManagerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    private var dsiEMVManager: DsiEMVManager? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun getName(): String = "DsiEMVManager"
    
    @ReactMethod
    fun initialize(config: ReadableMap, promise: Promise) {
        try {
            Log.d("DsiEMVManagerModule", "Initializing with config: ${config.toHashMap()}")
            
            val configFactory = POSConfigFactory.processMap(config)
            val emvCommunicator = object : EMVTransactionCommunicator {
                override fun onError(error: ErrorResponse) {
                    Log.e("DsiEMVManagerModule", "Error: $error")
                }

                override fun onCardReadSuccessfully(cardData: CardData) {
                    Log.d("DsiEMVManagerModule", "Card read successfully: $cardData")
                }

                override fun onSaleTransactionCompleted(saleDetails: SaleTransactionResponse) {
                    Log.d("DsiEMVManagerModule", "Sale completed: $saleDetails")
                }

                override fun onRecurringSaleCompleted(recurringDetails: RecurringTransactionResponse) {
                    Log.d("DsiEMVManagerModule", "Recurring sale completed: $recurringDetails")
                }

                override fun onCardReplaceTransactionCompleted(zeroAuthData: ZeroAuthTransactionResponse) {
                    Log.d("DsiEMVManagerModule", "Card replace completed: $zeroAuthData")
                }

                override fun onClientVersionCompleted(clientVersionDetails: ClientVersionResponse) {
                    Log.d("DsiEMVManagerModule", "Client version: $clientVersionDetails")
                }

                override fun onShowMessage(message: String) {
                    Log.d("DsiEMVManagerModule", "Message: $message")
                }
            }

            val configCommunicator = object : ConfigurationCommunicator {
                override fun onConfigError(errorMessage: String) {
                    Log.e("DsiEMVManagerModule", "Config error: $errorMessage")
                }

                override fun onConfigPingFailed() {
                    Log.e("DsiEMVManagerModule", "Config ping failed")
                }

                override fun onConfigPingSuccess() {
                    Log.d("DsiEMVManagerModule", "Config ping success")
                }

                override fun onConfigCompleted() {
                    Log.d("DsiEMVManagerModule", "Config completed")
                }
            }

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
                    promise.resolve("Sale transaction initiated")
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
                    promise.resolve("Recurring sale transaction initiated")
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
                    promise.resolve("Transaction cancelled")
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
                    manager.runTransaction(com.quivio.emvhandheldlib.models.Operation.DownloadConfig, com.quivio.emvhandheldlib.models.Operation.NONE)
                    promise.resolve("Config download initiated")
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
                    promise.resolve("Client version request initiated")
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
                    promise.resolve("Prepaid card read initiated")
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
}
