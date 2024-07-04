package br.com.uol.pagbank.plugpagservice.demo.model

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag

enum class PaymentType(
    val value: Int
) {
    DEBIT(PlugPag.TYPE_DEBITO),
    CREDIT(PlugPag.TYPE_CREDITO),
    VOUCHER(PlugPag.TYPE_VOUCHER),
    PIX(PlugPag.TYPE_PIX),
}
