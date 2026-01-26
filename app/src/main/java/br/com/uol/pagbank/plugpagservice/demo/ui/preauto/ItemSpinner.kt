package br.com.uol.pagbank.plugpagservice.demo.ui.preauto

import br.com.uol.pagbank.plugpagservice.demo.model.InstallmentType

data class ItemSpinner(
    val text: String,
    val type: InstallmentType
) {
    override fun toString(): String = text
}
