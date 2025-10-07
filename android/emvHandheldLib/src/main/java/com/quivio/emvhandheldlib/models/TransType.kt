package com.quivio.emvhandheldlib.models

enum class TransType {
    EMVParamDownload,
    EMVPadReset,
    EMVSale,
    GetPrePaidStripeAllTracks,
    EMVZeroAuth,
    ClientVersion
}