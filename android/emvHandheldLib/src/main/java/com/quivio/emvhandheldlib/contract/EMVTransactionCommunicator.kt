package com.quivio.emvhandheldlib.contract

import com.quivio.emvhandheldlib.models.CardData
import com.quivio.emvhandheldlib.models.ClientVersionResponse
import com.quivio.emvhandheldlib.models.ErrorResponse
import com.quivio.emvhandheldlib.models.RecurringTransactionResponse
import com.quivio.emvhandheldlib.models.SaleTransactionResponse
import com.quivio.emvhandheldlib.models.ZeroAuthTransactionResponse

interface EMVTransactionCommunicator {
    fun onError(error: ErrorResponse)
    fun onCardReadSuccessfully(cardData: CardData)
    fun onSaleTransactionCompleted(saleDetails: SaleTransactionResponse)
    fun onRecurringSaleCompleted(recurringDetails: RecurringTransactionResponse)
    fun onCardReplaceTransactionCompleted(zeroAuthData: ZeroAuthTransactionResponse)
    fun onClientVersionCompleted(clientVersionDetails: ClientVersionResponse)
    fun onShowMessage(message: String)
}