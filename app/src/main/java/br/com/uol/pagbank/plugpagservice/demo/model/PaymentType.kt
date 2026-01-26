package br.com.uol.pagbank.plugpagservice.demo.model

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag

/**
 * Os Tipos de pagamento
 * Listados na hora que o usuario vai fazer o pagamento
 */
enum class PaymentType(
    val value: Int
) {
    DEBIT(PlugPag.TYPE_DEBITO),
    CREDIT(PlugPag.TYPE_CREDITO),
    VOUCHER(PlugPag.TYPE_VOUCHER),
    PIX(PlugPag.TYPE_PIX),
    PRE_AUTO_CARD(PlugPag.TYPE_PREAUTO_CARD),
    PRE_AUTO_KEYED(PlugPag.TYPE_PREAUTO_KEYED),
}
