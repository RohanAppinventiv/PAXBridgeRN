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

class POSTransactionExecutor(context: Context, posConfig: ConfigFactory) {

    private val dsiEMVAndroidLib  by lazy {
        DsiEMVInstanceBuilder.getInstance(context)
    }
    private val requestBuilder by lazy {
        DsiEMVRequestBuilder(posConfig)
    }

    suspend fun doSale(amount: String) {
        withContext(Dispatchers.IO){
            val request = requestBuilder.buildEMVSaleRequest(amount)
            Log.d(PRINT_TAG, "Sale request prepared: $request")
            dsiEMVAndroidLib.ProcessTransaction(request)
        }
    }

    suspend fun doRecurringSale(amount: String) {
        withContext(Dispatchers.IO){
            val request = requestBuilder.buildEMVRecurringSaleRequest(amount)
            Log.d(PRINT_TAG, "Recurring Sale request prepared: $request")
            dsiEMVAndroidLib.ProcessTransaction(request)
        }
    }

    suspend fun doReplaceCardInRecurring() {
        withContext(Dispatchers.IO){
            val request = requestBuilder.buildReplaceCardInRecurringRequest()
            Log.d(PRINT_TAG, "Replace Card in Recurring request prepared: $request")
            dsiEMVAndroidLib.ProcessTransaction(request)
        }
    }

    suspend fun cancelTransaction(){
        dsiEMVAndroidLib.CancelRequest()
    }

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

    suspend fun readPrepaidCard(){
        withContext(Dispatchers.IO){
            Log.d(PRINT_TAG, "Inside readPrepaidCard()")
            dsiEMVAndroidLib.ProcessTransaction(
                requestBuilder.buildCollectCardDataRequest()
            )
        }
    }

    fun addPosTransactionListener(callback: ProcessTransactionResponseListener){
        dsiEMVAndroidLib.AddProcessTransactionResponseListener(callback)
    }

    fun clearAllListeners(){
        dsiEMVAndroidLib.ClearProcessTransactionResponseListeners()
        dsiEMVAndroidLib.ClearCollectCardDataResponseListeners()
    }

}