
package com.paxbridgern.bridge

import android.util.Log
import com.facebook.react.bridge.ReadableMap
import com.quivio.emvhandheldlib.contract.ConfigFactory

/**
 * Factory class for creating POS (Point of Sale) configuration objects from React Native data.
 * 
 * This class handles the conversion of configuration data received from the React Native layer
 * into the format required by the EMV handheld library. It provides validation and error handling
 * for all required configuration parameters.
 * 
 * The configuration includes merchant information, device settings, and network connectivity
 * parameters necessary for payment processing operations.
 */
class POSConfigFactory {
    
    /**
     * Data Transfer Object (DTO) that implements the ConfigFactory interface.
     * 
     * This private inner class encapsulates all the configuration parameters required
     * for POS operations. It serves as a bridge between the React Native layer and
     * the EMV handheld library's configuration requirements.
     * 
     * @param merchantID The unique identifier for the merchant
     * @param onlineMerchantID The merchant ID used for online transactions
     * @param isSandBox Flag indicating whether to use sandbox (CERT) or production (PROD) environment
     * @param secureDeviceName The name/identifier of the secure terminal device
     * @param operatorID The unique identifier for the operator/employee
     * @param posPackageID The package identifier for the POS system
     * @param pinPadIPAddress The IP address of the PIN pad device
     * @param pinPadPort The port number for communicating with the PIN pad
     */
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
        /**
         * Public entry point for creating a ConfigFactory instance from React Native data.
         * 
         * This method processes a ReadableMap received from the React Native layer and converts
         * it into a ConfigFactory implementation. It performs validation on all required fields
         * and provides detailed logging for debugging purposes.
         * 
         * @param map The ReadableMap containing configuration data from React Native
         * @return A ConfigFactory instance ready for use with the EMV handheld library
         * @throws IllegalArgumentException if any required string fields are missing or null
         * 
         * Expected map structure:
         * - merchantID: String (required)
         * - onlineMerchantID: String (required)
         * - isSandBox: Boolean (optional, defaults to true)
         * - secureDeviceName: String (required)
         * - operatorID: String (required)
         * - posPackageID: String (required)
         * - pinPadIPAddress: String (required)
         * - pinPadPort: String (required)
         */
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

        /**
         * Safely extracts a string value from a ReadableMap with validation.
         * 
         * This extension function ensures that the specified key exists in the map and
         * contains a non-null string value. If either condition fails, an exception is thrown
         * with a descriptive error message.
         * 
         * @param key The key to look up in the ReadableMap
         * @return The string value associated with the key
         * @throws IllegalArgumentException if the key is missing or the value is null
         */
        private fun ReadableMap.getStringOrThrow(key: String): String =
            if (hasKey(key) && !isNull(key)) getString(key)!!
            else throw IllegalArgumentException("Missing or invalid key: $key")

        /**
         * Safely extracts a boolean value from a ReadableMap with a default fallback.
         * 
         * This extension function attempts to retrieve a boolean value for the specified key.
         * If the key is missing or the value is null, it returns the provided default value
         * instead of throwing an exception.
         * 
         * @param key The key to look up in the ReadableMap
         * @param default The default value to return if the key is missing or null
         * @return The boolean value associated with the key, or the default value
         */
        private fun ReadableMap.getBooleanOrDefault(key: String, default: Boolean): Boolean =
            if (hasKey(key) && !isNull(key)) getBoolean(key)
            else default
    }
}