package br.com.uol.pagbank.plugpagservice.demo.model

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag

enum class InstallmentType(
    val value: Int
) {
    A_VISTA(PlugPag.INSTALLMENT_TYPE_A_VISTA),
    PARC_VENDEDOR(PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR),
    PARC_COMPRADOR(PlugPag.INSTALLMENT_TYPE_PARC_COMPRADOR),
}
