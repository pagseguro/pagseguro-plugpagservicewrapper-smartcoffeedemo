package br.com.uol.pagbank.plugpagservice.demo.extensions

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult
import br.com.uol.pagseguro.plugpagservice.wrapper.data.result.PlugPagNFCInfosResultDirectly

fun PlugPagNFCResult.toStringFormatted(): String =
    """
        result: $result
        startslot: $startSlot
        endslot: $endSlot
        value: ${slots[startSlot]["data"]?.toUint32LE()}
    """.trimIndent()

fun PlugPagNFCInfosResultDirectly.toStringFormatted(): String =
    """
        serialInfo: ${serialNumber?.map { "[$it]" } ?: "<null>"}
        other: ${other?.copyOfRange(0, other!![0].toInt() + 1)?.map { "[$it]" } ?: "<null>"}
        cardType: $cardType
        result: $result
        CID: $cid
    """.trimIndent()

fun ByteArray.toUint32LE(): UInt? {
    if (this.size < 4) {
        return null
    }
    val value = sliceArray(0..3)
    return value[0].toUByte().toUInt() or
            (value[1].toUByte().toUInt() shl 8) or
            (value[2].toUByte().toUInt() shl 16) or
            (value[3].toUByte().toUInt() shl 24)
}
