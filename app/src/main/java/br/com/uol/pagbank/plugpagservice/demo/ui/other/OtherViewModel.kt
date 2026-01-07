package br.com.uol.pagbank.plugpagservice.demo.ui.other

import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagStyleData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagBeepData
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagLedData
import br.com.uol.pagseguro.plugpagservice.wrapper.deeplink.enums.DeepLink
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class OtherViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }

    private val assets: AssetManager by lazy {
        KoinPlatformTools.defaultContext().get().get<AssetManager>()
    }

    companion object {
        private val leds = arrayListOf(
            PlugPagLedData.LED_BLUE,
            PlugPagLedData.LED_YELLOW,
            PlugPagLedData.LED_GREEN,
            PlugPagLedData.LED_RED,
            PlugPagLedData.LED_OFF,
        )

        private const val TWO_SECONDS_DELAY = 2000L

        private const val FILE_NAME = "teste.jpg"
    }

    /**
     * Mensagem de evento exibida na tela
     */
    private val _eventTextResource = MutableLiveData<Int>().apply {
        value = R.string.waiting
    }
    val eventTextResource: LiveData<Int> = _eventTextResource

    /**
     * Mensagem de evento exibida na tela
     */
    private val _eventText = MutableLiveData<String>().apply {
        value = ""
    }
    val eventText: LiveData<String> = _eventText

    private val _eventModelText = MutableLiveData<String>().apply {
        value = ""
    }
    val eventModelText: LiveData<String> = _eventModelText

    private val _eventSerialNumber = MutableLiveData<String>().apply {
        value = ""
    }
    val eventSerialNumber: LiveData<String> = _eventSerialNumber

    init {
        resetMessage()
        fillTerminalInfo()
    }

    /**
     * Funções que configuram a mensagem em tela
     */
    private fun setMessage(message: Int) {
        viewModelScope.launch {
            _eventTextResource.value = message
        }
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

    private fun endMessage(printResult: PlugPagPrintResult) {
        if (printResult.result == PlugPag.RET_OK) {
            endMessage(R.string.success)
        } else {
            endMessage("${printResult.errorCode}\n${printResult.message}")
        }
    }

    /**
     * Maneira oficial de obter o modelo e serial number do terminal
     */
    private fun fillTerminalInfo() {
        viewModelScope.launch {
            _eventModelText.value = "Model: ${plugpag.getModel()}"
            _eventSerialNumber.value = "Serial Number:  ${plugpag.getSerialNumber()}"
        }
    }

    /**
     * Solicita ao serviço que reinicie o terminal
     */
    fun reboot() {
        viewModelScope.launch(Dispatchers.Default) {
            endMessage(R.string.other_rebooting)

            /**
             * Chama a função de reboot() do wrapper
             */
            plugpag.reboot()
        }
    }

    /**
     * Função que testa os beeps do terminal
     */
    fun beep() {
        viewModelScope.launch(Dispatchers.Default) {
            for (frequency in PlugPagBeepData.FREQUENCE_LEVEL_0..PlugPagBeepData.FREQUENCE_LEVEL_6) {
                viewModelScope.launch {
                    _eventTextResource.value = R.string.other_beeping
                }
                // faz o terminal emitir um beep
                plugpag.beep(
                    PlugPagBeepData(frequency.toByte(), 100)
                )
            }
            endMessage(R.string.success)
        }
    }

    /**
     * Função que testa os LEDs do terminal
     */
    fun led() {
        viewModelScope.launch(Dispatchers.Default) {
            for (led in leds) {
                viewModelScope.launch {
                    _eventTextResource.value = R.string.other_leding
                }
                /**
                 * Acende/apaga os leds de feedback
                 */
                plugpag.setLed(
                    PlugPagLedData(led)
                )
                delay(250)
            }
            endMessage(R.string.success)
        }
    }

    /**
     * Função que resgata e exibe os dados da ultima transação aprovada
     */
    fun lastTransaction() {
        viewModelScope.launch(Dispatchers.Default) {
            /**
             *  Chama a função de que resgata os dados
             */
            val lastTransaction = plugpag.getLastApprovedTransaction()
            viewModelScope.launch {
                if (lastTransaction.result == null ||
                    lastTransaction.transactionCode?.isEmpty() != false ||
                    lastTransaction.transactionId?.isEmpty() != false ||
                    lastTransaction.amount?.isEmpty() != false
                ) {
                    _eventTextResource.value = R.string.other_get_last_transaction_no
                } else {
                    _eventText.value = "Result: '${lastTransaction.errorCode}'\n" +
                            "Code: '${
                                if (lastTransaction.transactionCode!!.length > 10)
                                    lastTransaction.transactionCode!!.substring(
                                        0,
                                        8
                                    ) + "..." else lastTransaction.transactionCode
                            }'\n" +
                            "Id: '${lastTransaction.transactionId}'\n" +
                            "Amount: ${"%.2f".format((lastTransaction.amount?.toInt() ?: 0) / 100f)}\n"
                }
            }
        }
    }

    /**
     * Reimprime a via do estabelecimento, da ultima transação aprovada
     */
    fun reprintEstablishmentReceipt() {
        viewModelScope.launch(Dispatchers.Default) {
            viewModelScope.launch {
                _eventTextResource.value = R.string.other_reprinting_establishment_receipt
            }
            /**
             *  Chama a função de reimpressão da via do estabelecimento
             */
            plugpag.reprintStablishmentReceipt()
            endMessage(R.string.success)
        }
    }

    /**
     * Reimprime a via do cliente, da ultima transação aprovada
     */
    fun reprintCustomerReceipt() {
        viewModelScope.launch(Dispatchers.Default) {
            viewModelScope.launch {
                _eventTextResource.value = R.string.other_reprinting_customer_receipt
            }
            // Chama a função de reimpressão da via do cliente
            plugpag.reprintCustomerReceipt()
            endMessage(R.string.success)
        }
    }

    private fun copyFile(inSt: InputStream?, outSt: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int? = null
        while (inSt?.read(buffer).also { read = it!! } != -1) {
            read?.let { outSt.write(buffer, 0, it) }
        }
    }

    /**
     *  Função de impressão livre de um arquivo interno
     */
    fun print(externalDir: String) {
        viewModelScope.launch(Dispatchers.IO) {

            setMessage(R.string.print_file_cheking)
            val pathExternal = "$externalDir/$FILE_NAME"
            val inStInternal = assets.open(FILE_NAME)

            val file = File(pathExternal)
            if (file.exists()) {
                setMessage(R.string.print_file_deleting)
                file.delete()
            }

            setMessage(R.string.print_file_copying)
            if (file.createNewFile()) {
                copyFile(
                    inStInternal,
                    FileOutputStream(file)
                )
            } else {
                endMessage(R.string.print_file_cant_create)
                return@launch
            }

            viewModelScope.launch(Dispatchers.Default) {
                setMessage(R.string.print_file_printing)
                try {

                    /**    Resultados que podem ser retornados em caso de erro:
                    //    # 5001 ou I001
                    //        Recibo em branco. - Verificar a imagem gerada.
                    //    - "5002" ou "I002""
                    //        Impressora sem papel. - Trocar a bobina no terminal
                    //    - "5003" ou "I003"
                    //        Impressora superaquecida.
                    //        Verificar as imagens que estão sendo impressas e colocar delay entre impressões em loop
                    //    - "5004" ou "I004"
                    //        Impressora sob baixa tensão (bateria fraca).
                    //        Conectar o terminal na energia para carregar
                    //    - "5005" ou "I005"
                    //        Impressora ocupada. - Aguardar a finalização de uma impressão ou reiniciar o terminal
                    //    - "5006" ou "I006"
                    //        Formato de pacote de dados invalido.
                    //        Reiniciar o terminal e verificar se a imagem está correta
                    //    - "5007" ou "I007"
                    //        Impressora com mau funcionamento. - Reiniciar o terminal e tentar novamente
                    //    - "5008" ou "I008"
                    //        Impressão inacabada.
                    //        Reiniciar o terminal e tentar novamente
                    //    - "5009" ou "I009"
                    //        Impressora sem biblioteca de fontes instalada.
                    //        Informar o PagBank sobre o problema com exemplo para que seja analisado a imagem
                    //    - "5010" ou "I010"
                    //        O pacote de dados para impressão excedeu o limite de tamanho permitido.
                    //        Reduzir o tamanho da imagem e tentar novamente
                    //    - "5011" ou "I011"
                    //        Arquivo para impressão não encontrado.
                    //        O arquivo não existe
                    //    - "5013" ou "I013"
                    //        O arquivo informado não é válido.
                    //        O arquivo não é uma imagem
                    //    - "5014" ou "I014"
                    //        Não foi possível processar a imagem. Tente novamente.
                    //        O serviço não possui permissão para para acessar o arquivo ou não foi possível realizar o decode da imagem,
                    //        verifique se seguiu os passos da documentação e possui suporte a AndroidX
                     */

                    val result = plugpag.printFromFile(
                        PlugPagPrinterData(
                            file.absolutePath,
                            printerQuality = 4,
                            steps = 0
                        )
                    )

                    endMessage(result)
                } catch (e: PlugPagException) {
                    endMessage(e.message ?: "Erro")
                }
            }
        }
    }

    /**
     *  Desfaz a ultima transação
     */
    fun undoLastTransaction() {
        viewModelScope.launch(Dispatchers.Default) {
            viewModelScope.launch {
                _eventTextResource.value = R.string.other_undoing_last_transaction
            }

            /**
             * Resgata os dados da ultima transação aprovada
             */
            val lastTransaction = plugpag.getLastApprovedTransaction()
            if (lastTransaction.result == null) {
                _eventTextResource.value = R.string.other_get_last_transaction_no
            } else {
                /**
                 *  Recebe os eventos de mensagens do serviço para instruir as ações do usuário
                 */
                plugpag.setEventListener(object : PlugPagEventListener {
                    override fun onEvent(data: PlugPagEventData) {
                        data.customMessage?.let {
                            _eventText.value = it
                        }
                    }
                })

                /**
                 * Configura o popup de impressão da via do cliente
                 */
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
                        60, // tempo de espera máximo do popup de impressão
                    )
                )

                /**
                 *  Inicia o processo de estorno da transação da combinação de transactionCode e transactionId informados
                 *  o resultado do estorno é retornado para posterior tratativa
                 */
                val result = plugpag.voidPayment(
                    PlugPagVoidData(
                        transactionCode = lastTransaction.transactionCode!!,
                        transactionId = lastTransaction.transactionId!!,
                        voidType =
                            if (lastTransaction.paymentType == PlugPag.TYPE_PIX ||
                                lastTransaction.paymentType == PlugPag.TYPE_QRCODE ||
                                lastTransaction.paymentType == PlugPag.TYPE_QRCODE_CREDITO
                            ) {
                                PlugPag.VOID_QRCODE
                            } else {
                                PlugPag.VOID_PAYMENT
                            }
                    )
                )

                /** Passa o resultado da transação para a UI
                 *  os resultados mais comuns encontrados no result.errorCode são, mas não apenas:
                 *  # "0000"
                 *      para estornos aprovados
                 *  - "C13" ou "B018"
                 *      para estornos cancelados pelo usuário
                 *  - "M512" ou "M3005" ou "M3025" ou "M5001"
                 *      para estornos cancelados pelas regras do PagBank
                 *  - "C81" ou "C83"
                 *      se houver erro na comunicação com o cartão aproximado
                 *  - "C12"
                 *      se o tempo esperando o cartão acabar
                 *  - "C70" ou "C84"
                 *      se o tipo do cartão inserido for diferente do tipo selecionado
                 *  - "PP1051"
                 *      se o cartão usado é diferente do cartão da transação
                 *  - "C60" ou "C61"
                 *      se houver um erro na leitura do cartão, pedindo para o usuário tentar novamente
                 *  - "SV03"
                 *      se o serviço de pagamento estiver ocupado
                 */
                if (result.result == PlugPag.RET_OK) {
                    endMessage(R.string.success)
                } else {
                    endMessage("${result.errorCode}\n${result.message}")
                }
            }
        }
    }

    /**
     *  Função para ajustar as cores da PPS
     *  das telas personalizadas dos fluxos transacionais
     */
    fun setStyleData() {
        viewModelScope.launch(Dispatchers.Default) {
            val randomStyleData = PlugPagStyleData(
                genericButtonBackground = Color.BLUE,
                genericButtonTextColor = Color.WHITE
            )

            runCatching {
                if (plugpag.setStyleData(randomStyleData)) {
                    _eventText.postValue("Cores definidas com sucesso.")
                }
            }.onFailure {
                _eventText.postValue("Falha na definição de cores.")
            }
        }
    }

    /**
     *   Disponivel apenas apartir da versão 2.2.0 do Launcher PagBank
     */
    fun setDeepLink() {
        viewModelScope.launch(Dispatchers.IO) {
            plugpag.setDeeplink(
                deeplink = DeepLink.SELL,
                uri = "app://demopps/home"
            )
            _eventText.postValue("DeepLink configurado")
        }
    }

    fun removeDeeplink() {
        viewModelScope.launch(Dispatchers.IO) {
            plugpag.removeDeeplink(
                deeplink = DeepLink.SELL
            )
            _eventText.postValue("DeepLink removido")
        }
    }

    /**
     *  Abrir a loja de apps em um app especifico
     */
    fun createInstallIntent(): Intent {
        val appPackage = "br.com.uol.pagseguro.plugpagservice"
        val url = String.format("market://detail?id=%s", appPackage)
        val uri: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setClassName(
            "com.pax.market.android.app",
            "com.pax.market.android.app.presentation.search.view.activity.SearchAppDetailActivity"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

}
