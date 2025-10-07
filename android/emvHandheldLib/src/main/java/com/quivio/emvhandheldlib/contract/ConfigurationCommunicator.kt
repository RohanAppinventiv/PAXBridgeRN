package com.quivio.emvhandheldlib.contract

interface ConfigurationCommunicator {
    fun onConfigError(errorMessage: String)
    fun onConfigPingFailed()
    fun onConfigPingSuccess()
    fun onConfigCompleted()
}