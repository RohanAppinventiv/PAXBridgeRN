
package com.paxbridgern.bridge

import android.util.Log
import com.facebook.react.bridge.ReadableMap
import com.quivio.emvhandheldlib.contract.ConfigFactory

class POSConfigFactory {
    private class ConfigDTO(
        override val merchantID: String,
        override val onlineMerchantID: String,
        override val isSandBox: Boolean, // true then "CERT" else "PROD"
        override val secureDeviceName: String, // Terminal device name
        override val operatorID: String, // employee id
        override val posPackageID: String, // POS package ID
        override val pinPadIPAddress: String, // PinPad IP address
        override val pinPadPort: String // PinPad port
    ): ConfigFactory

    companion object {
        /** Public entry point: build from RN's ReadableMap */
        fun processMap(map: ReadableMap): ConfigFactory {
            Log.d("POSConfigFactory", "Processing map with keys: ${map.toHashMap().keys}")
            Log.d("POSConfigFactory", "posPackageID present: ${map.hasKey("posPackageID")}")
            if (map.hasKey("posPackageID")) {
                Log.d("POSConfigFactory", "posPackageID value: ${map.getString("posPackageID")}")
            }
            
            return ConfigDTO(
                merchantID       = map.getStringOrThrow("merchantID"),
                onlineMerchantID = map.getStringOrThrow("onlineMerchantID"),
                isSandBox        = map.getBooleanOrDefault("isSandBox", true),
                secureDeviceName = map.getStringOrThrow("secureDeviceName"),
                operatorID       = map.getStringOrThrow("operatorID"),
                posPackageID     = map.getStringOrThrow("posPackageID"),
                pinPadIPAddress  = map.getStringOrThrow("pinPadIPAddress"),
                pinPadPort       = map.getStringOrThrow("pinPadPort")
            )
        }

        /** Throws if the key is missing or not a String */
        private fun ReadableMap.getStringOrThrow(key: String): String =
            if (hasKey(key) && !isNull(key)) getString(key)!!
            else throw IllegalArgumentException("Missing or invalid key: $key")

        /** Returns the boolean value if present, otherwise `default` */
        private fun ReadableMap.getBooleanOrDefault(key: String, default: Boolean): Boolean =
            if (hasKey(key) && !isNull(key)) getBoolean(key)
            else default
    }
}