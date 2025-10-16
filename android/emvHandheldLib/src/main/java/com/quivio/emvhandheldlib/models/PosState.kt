package com.quivio.emvhandheldlib.models

/**
 * Sealed class representing the current state of the POS (Point of Sale) system.
 * 
 * This class uses the State pattern to track the current operation being performed
 * and the next operation to be executed. It helps manage the flow of EMV transactions
 * and ensures proper sequencing of operations.
 * 
 * The sealed class design ensures type safety and exhaustive handling of all
 * possible states in the POS system.
 * 
 * @param currentOperation The operation currently being executed
 * @param nextOperation The operation to be executed after the current one completes
 */
sealed class PosState(val currentOperation: Operation, val nextOperation: Operation) {
    /**
     * Represents the idle state when no operations are running.
     * 
     * This state indicates that the POS system is ready to accept new operations
     * and no transactions are currently in progress.
     */
    object IDLE : PosState(Operation.NONE, Operation.NONE)
    
    /**
     * Represents the running state when an operation is in progress.
     * 
     * This state indicates that a specific operation is currently being executed
     * and specifies what operation should be performed next upon completion.
     * 
     * @param current The operation currently being executed
     * @param next The operation to be executed after the current one completes
     */
    data class Running(val current: Operation, val next: Operation) : PosState(current, next)
}

/**
 * Enumeration of all possible operations that can be performed by the POS system.
 * 
 * This enum defines all the different types of operations that the EMV payment
 * system can execute, including transactions, configuration, and maintenance operations.
 * 
 * Each operation represents a specific action that can be taken by the POS system,
 * and the state machine uses these operations to manage the flow of execution.
 */
enum class Operation {
    /** No operation - used for idle state or completion */
    NONE,
    
    /** Reset the PIN pad device to clear any pending operations */
    Reset,
    
    /** Download configuration parameters from the payment processor */
    DownloadConfig,
    
    /** Execute a standard EMV sale transaction */
    EMVSale,
    
    /** Execute a recurring EMV sale transaction */
    RecurringSale,
    
    /** Replace a card in a recurring payment setup */
    ReplaceCard,
    
    /** Retrieve client version information */
    GetClientVersion,
    
    /** Read data from a prepaid card */
    ReadPrepaidCard
}