package com.quivio.emvhandheldlib.contract

/**
 * Configuration factory interface for POS (Point of Sale) system settings.
 * 
 * This interface defines the contract for providing configuration data required
 * for EMV payment processing operations. It encapsulates all the necessary
 * merchant, device, and network configuration parameters needed to establish
 * communication with the payment processing system.
 * 
 * The interface is implemented by configuration classes that provide these
 * settings from various sources (e.g., React Native configuration, local storage,
 * or remote configuration services).
 * 
 * All properties are read-only to ensure configuration immutability once set.
 */
interface ConfigFactory {
    /** The unique identifier for the merchant in the payment system */
    val merchantID: String
    
    /** The merchant identifier used for online/web-based transactions */
    val onlineMerchantID: String
    
    /** Flag indicating whether to use sandbox (true) or production (false) environment */
    val isSandBox: Boolean
    
    /** The name/identifier of the secure terminal device */
    val secureDeviceName: String
    
    /** The unique identifier for the operator/employee performing transactions */
    val operatorID: String
    
    /** The package identifier for the POS system */
    val posPackageID: String
    
    /** The IP address of the PIN pad device for communication */
    val pinPadIPAddress: String
    
    /** The port number for communicating with the PIN pad device */
    val pinPadPort: String
}