package br.com.uol.pagbank.plugpagservice.demo.ui.other

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagBeepData
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagLedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

class OtherViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }

    companion object {
        private val leds = arrayListOf(
            PlugPagLedData.LED_BLUE,
            PlugPagLedData.LED_YELLOW,
            PlugPagLedData.LED_GREEN,
            PlugPagLedData.LED_RED,
            PlugPagLedData.LED_OFF,
        )

        private const val TWO_SECONDS_DELAY = 2000L
    }

    // Event message resource
    private val _eventTextResource = MutableLiveData<Int>().apply {
        value = R.string.waiting
    }
    val eventTextResource: LiveData<Int> = _eventTextResource

    // Event message
    private val _eventText = MutableLiveData<String>().apply {
        value = ""
    }
    val eventText: LiveData<String> = _eventText

    init {
        resetMessage()
    }

    private fun resetMessage() {
        viewModelScope.launch {
            _eventTextResource.value = R.string.waiting
        }
    }

    private fun endMessage(message: Int) {
        viewModelScope.launch {
            _eventTextResource.value = message
            delay(TWO_SECONDS_DELAY)
            resetMessage()
        }
    }

    private fun endMessage(message: String) {
        viewModelScope.launch {
            _eventText.value = message
            delay(TWO_SECONDS_DELAY)
            resetMessage()
        }
    }

    fun reboot() {
        viewModelScope.launch(Dispatchers.Default) {
            endMessage(R.string.other_rebooting)
            // solicita ao serviço que reinicie o terminal
            plugpag.reboot()
        }
    }

    fun beep() {
        viewModelScope.launch(Dispatchers.Default) {
            for (frequency in PlugPagBeepData.FREQUENCE_LEVEL_0..PlugPagBeepData.FREQUENCE_LEVEL_6) {
                viewModelScope.launch {
                    _eventTextResource.value = R.string.other_beeping
                }
                // faz o terminal emitir um beep
                plugpag.beep(
                    PlugPagBeepData(frequency.toByte(), 1)
                )
            }
            endMessage(R.string.success)
        }
    }

    fun led() {
        viewModelScope.launch(Dispatchers.Default) {
            for (led in leds) {
                viewModelScope.launch {
                    _eventTextResource.value = R.string.other_leding
                }
                // acende/apaga os leds de feedback
                plugpag.setLed(
                    PlugPagLedData(led)
                )
                delay(250)
            }
            endMessage(R.string.success)
        }
    }

    fun lastTransaction() {
        viewModelScope.launch(Dispatchers.Default) {
            // resgata os dados a ultima transação aprovada
            val lastTransaction = plugpag.getLastApprovedTransaction()
            viewModelScope.launch {
                if (lastTransaction.result == null ||
                    lastTransaction.transactionCode?.isEmpty() != false ||
                    lastTransaction.transactionId?.isEmpty() != false ||
                    lastTransaction.amount?.isEmpty() != false) {
                    _eventTextResource.value = R.string.other_get_last_transaction_no
                } else {
                    _eventText.value = "Result: '${lastTransaction.errorCode}'\n" +
                            "Code: '${if (lastTransaction.transactionCode!!.length > 10)
                                lastTransaction.transactionCode!!.substring(0, 8) + "..." else lastTransaction.transactionCode}'\n" +
                            "Id: '${lastTransaction.transactionId}'\n" +
                            "Amount: ${"%.2f".format((lastTransaction.amount?.toInt() ?: 0) / 100f)}\n"
                }
            }
        }
    }

    fun reprintEstablishmentReceipt() {
        viewModelScope.launch(Dispatchers.Default) {
            viewModelScope.launch {
                _eventTextResource.value = R.string.other_reprinting_establishment_receipt
            }
            // reimprime a via do estabelecimento
            plugpag.reprintStablishmentReceipt()
            endMessage(R.string.success)
        }
    }

    fun reprintCustomerReceipt() {
        viewModelScope.launch(Dispatchers.Default) {
            viewModelScope.launch {
                _eventTextResource.value = R.string.other_reprinting_customer_receipt
            }
            // reimprime a via do cliente
            plugpag.reprintCustomerReceipt()
            endMessage(R.string.success)
        }
    }

    fun undoLastTransaction() {
        viewModelScope.launch(Dispatchers.Default) {
            viewModelScope.launch {
                _eventTextResource.value = R.string.other_undoing_last_transaction
            }

            // resgata os dados a ultima transação aprovada
            val lastTransaction = plugpag.getLastApprovedTransaction()
            if (lastTransaction.result == null) {
                _eventTextResource.value = R.string.other_get_last_transaction_no
            } else {
                // recebe os eventos de mensagens do serviço para instruir as ações do usuário
                plugpag.setEventListener(object : PlugPagEventListener {
                    override fun onEvent(data: PlugPagEventData) {
                        data.customMessage?.let {
                            _eventText.value = it
                        }
                    }
                })

                // configura o popup de impressão da via do cliente
                plugpag.setPlugPagCustomPrinterLayout(
                    PlugPagCustomPrinterLayout(
                        "Imprimir via do cliente?",
                        "#000000",
                        "#FFFFFF",
                        "#A0A0A0",
                        "#FFFFFF",
                        "#000000",
                        "#808080",
                        "#FFFFFF",
                        60, // time out in seconds
                    )
                )

                // inicia o processo de estorno da transação da combinação de transactionCode e transactionId informados
                // o resultado do estorno é retornado para posterior tratativa
                val result = plugpag.voidPayment(
                    PlugPagVoidData(
                        transactionCode = lastTransaction.transactionCode!!,
                        transactionId = lastTransaction.transactionId!!,
                        voidType =
                        if (lastTransaction.paymentType == PlugPag.TYPE_PIX ||
                            lastTransaction.paymentType == PlugPag.TYPE_QRCODE ||
                            lastTransaction.paymentType == PlugPag.TYPE_QRCODE_CREDITO) {
                            PlugPag.VOID_QRCODE
                        } else {
                            PlugPag.VOID_PAYMENT
                        }
                    )
                )

                // passa o resultado da transação para a UI
                // os resultados mais comuns encontrados no result.errorCode são, mas não apenas:
                //  # "0000"
                //      para estornos aprovados
                //  - "C13" ou "B018"
                //      para estornos cancelados pelo usuário
                //  - "M512" ou "M3005" ou "M3025" ou "M5001"
                //      para estornos cancelados pelas regras do PagBank
                //  - "C81" ou "C83"
                //      se houver erro na comunicação com o cartão aproximado
                //  - "C12"
                //      se o tempo esperando o cartão acabar
                //  - "C70" ou "C84"
                //      se o tipo do cartão inserido for diferente do tipo selecionado
                //  - "PP1051"
                //      se o cartão usado é diferente do cartão da transação
                //  - "C60" ou "C61"
                //      se houver um erro na leitura do cartão, pedindo para o usuário tentar novamente
                //  - "SV03"
                //      se o serviço de pagamento estiver ocupado
                if (result.result == PlugPag.RET_OK) {
                    endMessage(R.string.success)
                } else {
                    endMessage("${result.errorCode}\n${result.message}")
                }
            }
        }
    }
}
