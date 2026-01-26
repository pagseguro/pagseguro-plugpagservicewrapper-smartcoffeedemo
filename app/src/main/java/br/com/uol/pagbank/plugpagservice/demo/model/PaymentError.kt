package br.com.uol.pagbank.plugpagservice.demo.model

/**
 * Os possiveis erros no fluxo de pagamento
 */
enum class PaymentError {
    INVALID_AMOUNT,
    INVALID_INSTALLMENTS,
    INVALID_STATE,
    INVALID_SETUP,
    EMPTY_VALUE,
}
