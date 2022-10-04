package br.com.uol.pagseguro.smartcoffee.payments.preauto

import java.io.Serializable

class PreAutoPayment(
    var installmentNumber: Int,
    var pan: String,
    var expirationDate: String,
    var cardCvv: String,
    var transactionCode: String,
    var transactionDate: String,
) : Serializable
