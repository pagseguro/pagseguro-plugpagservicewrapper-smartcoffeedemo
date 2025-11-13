package br.com.uol.pagbank.plugpagservice.demo.model

/**
 * Estados da tela de pagamento
 */
enum class PaymentState {
    GETTING_AMOUNT,
    GETTING_TYPE,
    GETTING_INSTALLMENT_TYPE,
    GETTING_INSTALLMENTS,
    PAYING,
    RESULT,
    GETTING_CARD_DATA,
}
