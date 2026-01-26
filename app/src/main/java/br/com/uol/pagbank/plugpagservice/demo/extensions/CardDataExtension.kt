package br.com.uol.pagbank.plugpagservice.demo.extensions

fun String.formatExpDate(): String {
        var expDate = this
        if (expDate.length == 4) {
            expDate = expDate.substring(2) + expDate.substring(0, 2)
        }
        return expDate
    }

fun String.formatTransactionDate(): String {
    var date = this
    if (date.length == 8) {
        val day = date.substring(0, 2)
        val month = date.substring(2, 4)
        val year = date.substring(4, 8)
        date = "$year-$month-$day"
    }
    return date
}
