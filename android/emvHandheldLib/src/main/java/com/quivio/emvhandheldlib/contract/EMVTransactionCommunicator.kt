package com.quivio.emvhandheldlib.contract

import com.quivio.emvhandheldlib.models.CardData
import com.quivio.emvhandheldlib.models.ClientVersionResponse
import com.quivio.emvhandheldlib.models.ErrorResponse
import com.quivio.emvhandheldlib.models.RecurringTransactionResponse
import com.quivio.emvhandheldlib.models.SaleTransactionResponse
import com.quivio.emvhandheldlib.models.ZeroAuthTransactionResponse

/**
 * Communication interface for EMV transaction-related events and callbacks.
 * 
 * This interface defines the contract for handling EMV payment transaction
 * operations and their outcomes. It provides callbacks for various transaction
 * states including success, errors, card reading, and system messages.
 * 
 * Implementations of this interface are responsible for handling transaction
 * events and notifying the appropriate components (e.g., UI, business logic)
 * about the status and results of payment operations.
 * 
 * This interface is typically implemented by classes that need to respond
 * to transaction events from the EMV library, such as React Native bridge modules
 * or payment processing controllers.
 */
interface EMVTransactionCommunicator {
    /**
     * Called when an EMV operation encounters an error.
     * 
     * This callback is triggered when any EMV operation fails due to various
     * reasons such as network issues, card problems, or system errors.
     * 
     * @param error The error response containing detailed error information
     */
    fun onError(error: ErrorResponse)
    
    /**
     * Called when a card is successfully read from the EMV device.
     * 
     * This callback is triggered when card data is successfully extracted
     * from a card inserted into the EMV device, typically for prepaid cards.
     * 
     * @param cardData The card data containing track information and status
     */
    fun onCardReadSuccessfully(cardData: CardData)
    
    /**
     * Called when a sale transaction is successfully completed.
     * 
     * This callback is triggered when a standard sale transaction is
     * successfully processed and approved by the payment processor.
     * 
     * @param saleDetails The sale transaction response containing all transaction details
     */
    fun onSaleTransactionCompleted(saleDetails: SaleTransactionResponse)
    
    /**
     * Called when a recurring sale transaction is successfully completed.
     * 
     * This callback is triggered when a recurring payment transaction is
     * successfully processed and approved by the payment processor.
     * 
     * @param recurringDetails The recurring transaction response containing transaction details
     */
    fun onRecurringSaleCompleted(recurringDetails: RecurringTransactionResponse)
    
    /**
     * Called when a card replacement transaction is successfully completed.
     * 
     * This callback is triggered when a card replacement (zero auth) transaction
     * is successfully processed, typically used for updating recurring payment methods.
     * 
     * @param zeroAuthData The zero auth transaction response containing replacement details
     */
    fun onCardReplaceTransactionCompleted(zeroAuthData: ZeroAuthTransactionResponse)
    
    /**
     * Called when client version information is successfully retrieved.
     * 
     * This callback is triggered when version information about the EMV client
     * library is successfully obtained, useful for debugging and compatibility checks.
     * 
     * @param clientVersionDetails The client version response containing version information
     */
    fun onClientVersionCompleted(clientVersionDetails: ClientVersionResponse)
    
    /**
     * Called when the EMV library needs to display a message to the user.
     * 
     * This callback is triggered when the EMV library needs to show status
     * messages, prompts, or instructions to the user during transaction processing.
     * 
     * @param message The message to display to the user
     */
    fun onShowMessage(message: String)
}