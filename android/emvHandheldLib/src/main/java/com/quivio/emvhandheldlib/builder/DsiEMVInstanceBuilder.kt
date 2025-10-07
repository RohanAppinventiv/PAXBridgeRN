package com.quivio.emvhandheldlib.builder

import android.annotation.SuppressLint
import android.content.Context
import com.datacap.android.dsiEMVAndroid


internal object DsiEMVInstanceBuilder {
    @SuppressLint("StaticFieldLeak")
    private var emvInstance: dsiEMVAndroid? = null

    private fun initialize(context: Context): dsiEMVAndroid? {
        if (emvInstance == null) {
            emvInstance = dsiEMVAndroid(context)
        }
        return emvInstance
    }

    fun getInstance(context: Context): dsiEMVAndroid {
        return initialize(context)
            ?: throw IllegalStateException("dsiEMVAndroidManager is not initialized. Call initialize(context) first.")
    }

}