package com.quivio.emvhandheldlib.contract

import com.quivio.emvhandheldlib.models.CardData
import com.quivio.emvhandheldlib.models.SaleTransactionResponse

interface BridgeCommunicator {
    fun onError(errorMessage: String)
    fun onCardReadSuccessfully(cardData: CardData)
    fun onSaleTransactionCompleted(saleDetails: SaleTransactionResponse)
    fun onShowMessage(message: String)
    fun onConfigError(errorMessage: String)
    fun onConfigPingFailed()
    fun onConfigPingSuccess()
    fun onConfigCompleted()
}