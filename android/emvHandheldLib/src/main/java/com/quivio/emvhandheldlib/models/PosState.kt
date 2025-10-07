package com.quivio.emvhandheldlib.models

sealed class PosState(val currentOperation: Operation, val nextOperation: Operation) {
    object IDLE : PosState (Operation.NONE, Operation.NONE)
    data class Running(val current: Operation, val next: Operation) : PosState(current, next)
}

enum class Operation {
    NONE,
    Reset,
    DownloadConfig,
    EMVSale,
    RecurringSale,
    ReplaceCard,
    GetClientVersion,
    ReadPrepaidCard
}