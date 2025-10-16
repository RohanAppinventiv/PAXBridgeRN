package com.quivio.emvhandheldlib.contract

/**
 * Communication interface for configuration-related events and callbacks.
 * 
 * This interface defines the contract for handling configuration operations
 * and their outcomes in the EMV payment system. It provides callbacks for
 * various configuration states including errors, ping tests, and completion
 * events.
 * 
 * Implementations of this interface are responsible for handling configuration
 * events and notifying the appropriate components (e.g., UI, logging systems)
 * about the status of configuration operations.
 * 
 * This interface is typically implemented by classes that need to respond
 * to configuration events from the EMV library.
 */
interface ConfigurationCommunicator {
    /**
     * Called when a configuration error occurs.
     * 
     * This callback is triggered when any configuration operation encounters
     * an error. The error message provides details about what went wrong.
     * 
     * @param errorMessage A descriptive error message explaining the configuration issue
     */
    fun onConfigError(errorMessage: String)
    
    /**
     * Called when a configuration ping test fails.
     * 
     * This callback is triggered when the system fails to establish communication
     * with the configuration server or when the ping test times out.
     */
    fun onConfigPingFailed()
    
    /**
     * Called when a configuration ping test succeeds.
     * 
     * This callback is triggered when the system successfully establishes
     * communication with the configuration server.
     */
    fun onConfigPingSuccess()
    
    /**
     * Called when configuration setup is fully completed.
     * 
     * This callback is triggered when all configuration operations have been
     * successfully completed and the system is ready for payment processing.
     */
    fun onConfigCompleted()
}