package br.com.uol.pagbank.plugpagservice.demo.ui.nfc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.extensions.toStringFormatted
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCDetectRemoveCard
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldRemoveCardType
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.EM1KeyType
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuth
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuthDirectly
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagSimpleNFCData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

/**
 * Atualmente apenas NFC Mifare Classic são suportados
 * Antes de fazer qualquer operação do Mifare é importante ligar a antena
 * e após finalizar as operações desligar (startDirectly() e stopDirectly())
 */
class NFCViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }

    companion object {
        private const val TIMEOUT = 10 // em segundos
        private const val DEMO_SLOT = 18 // index
        private const val DEMO_SLOT_DEST = 17 // index
        private const val KEY_DATA = "data"
        private const val KEY_PASS = "pwd"

        private val DEFAULT_KEY_NFC = byteArrayOf(
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte()
        )

        private val VALUE_100_LE = byteArrayOf(0x64, 0x00, 0x00, 0x00)
        private val VALUE_50_LE = byteArrayOf(0x32, 0x00, 0x00, 0x00)

        /**
         * Tamanho = 16
         */
        private val VALUE_100_SLOT = byteArrayOf(
            0x64, 0x00, 0x00, 0x00, // value (litter-endian)
            0x9B.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), // ~value
            0x64, 0x00, 0x00, 0x00, // value (litter-endian)
            0x05, 0xFA.toByte(), 0x05, 0xFA.toByte(), // ADDR/~ADDR
        )
    }

    /**
     * Menssagem de evento (resource)
     */
    private val _eventTextMessage = MutableLiveData<String>().apply {
        value = "Aguardando"
    }
    val eventTextResource: LiveData<String> = _eventTextMessage

    private val _state = MutableLiveData<NFCState>().apply {
        value = NFCState.IDLE
    }

    val state: LiveData<NFCState> = _state

    /**
     * Liga a antena NFC e ativa o cartão
     */
    @Throws(Exception::class)
    private fun startDirectly() {
        val result = plugpag.startNFCCardDirectly()
        if (result != PlugPag.NFC_RET_OK) {
            throw Exception("Erro ao ligar a antena")
        }
    }

    /**
     *  Desliga a antena NFC e desativa o cartão
     */
    @Throws(Exception::class)
    private fun stopDirectly() {
        val result = plugpag.stopNFCCardDirectly()
        if (result != PlugPag.NFC_RET_OK) {
            throw Exception("Erro ao desligar a antena")
        }
    }

    /**
     *  Aborta o processo de detecção
     */
    fun abort() {
        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                plugpag.abortNFC()
            }
        }
    }

    /**
     *  Detecta se há um cartão próximo à antena, se houver retorna seu serial
     */
    fun detectDirectly() {
        performAction {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK) {
                throw Exception("Cartão não identificado")
            }

            detect.toStringFormatted()
        }
    }

    /**
     * Detecta se há um cartão próximo à antena, se houver retorna seu serial
     */
    fun removeDirectly() {

        performAction {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                0
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.cid == null) {
                throw Exception("Cartão não identificado")
            }

            val detectRemove = PlugPagNFCDetectRemoveCard(
                PlugPagNearFieldRemoveCardType.REMOVE,
                detect.cid!!
            )

            if (plugpag.detectNfcRemoveDirectly(detectRemove) != PlugPag.NFC_RET_OK) {
                throw Exception("Cartão não removido")
            }

            "Cartão removido"
        }
    }

    /**
     * Autentica o cartão, é necessário que a antena já esteja ligada e o cartão ativado
     */
    fun authDirectly() {
        performAction {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.serialNumber == null) {
                throw Exception("Cartão não identificado")
            }

            val cardData = PlugPagNFCAuthDirectly(
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A,
                detect.serialNumber!!
            )

            val auth = plugpag.justAuthNfcDirectly(cardData)

            if (auth == PlugPag.NFC_RET_OK) {
                "Cartão autenticado"
            } else {
                "Cartão não autenticado"
            }
        }
    }

    /**
     *  Lê o cartão, é necessário que a antena já esteja ligada e o cartão ativado
     */
    fun readDirectly() {

        performAction {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.serialNumber == null) {
                throw Exception("Cartão não identificado")
            }

            val authData = PlugPagNFCAuthDirectly(
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A,
                detect.serialNumber!!
            )

            val auth = plugpag.justAuthNfcDirectly(authData)

            if (auth != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao autenticar")
            }

            val cardData = PlugPagSimpleNFCData(
                EM1KeyType.TYPE_A.ordinal,
                DEMO_SLOT,
                ByteArray(16)
            )

            val readData = plugpag.readNFCCardDirectly(cardData)

            if (readData.result != PlugPag.NFC_RET_OK || readData.slots[readData.startSlot][KEY_DATA] == null) {
                throw Exception("Falha ao ler o cartão")
            }

            readData.toStringFormatted()
        }
    }

    /** Escreve no cartão
     *  é necessário que a antena já esteja ligada e o cartão ativado
     */
    fun writeDirectly() {

        performAction {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.serialNumber == null) {
                throw Exception("Cartão não identificado")
            }

            val authData = PlugPagNFCAuthDirectly(
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A,
                detect.serialNumber!!
            )

            val auth = plugpag.justAuthNfcDirectly(authData)

            if (auth != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao autenticar")
            }

            val cardData = PlugPagSimpleNFCData(
                EM1KeyType.TYPE_A.ordinal,
                DEMO_SLOT,
                VALUE_100_SLOT
            )

            val result = plugpag.writeToNFCCardDirectly(cardData)

            if (result != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao escrever no cartão")
            }

            "Escrita realizada com sucesso"
        }
    }

    /**
     * Ação de leitura, controlada pela aplicação PagBank
     */
    fun readPagBank() {
        performAction {
            val cardData = PlugPagNearFieldCardData().apply {
                startSlot = DEMO_SLOT
                endSlot = DEMO_SLOT
                slots[DEMO_SLOT][KEY_PASS] = DEFAULT_KEY_NFC
                timeOutRead = TIMEOUT
            }

            val readData = plugpag.readFromNFCCard(cardData)

            if (readData.result != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao ler o cartão")
            }

            readData.toStringFormatted()
        }
    }

    /**
     * Ação de escrita, controlada pela pps
     */
    fun writePagBank() {
        performAction {
            val cardData = PlugPagNearFieldCardData().apply {
                startSlot = DEMO_SLOT
                endSlot = DEMO_SLOT
                slots[DEMO_SLOT][KEY_PASS] = DEFAULT_KEY_NFC
                slots[DEMO_SLOT][KEY_DATA] = VALUE_100_SLOT
                timeOutRead = TIMEOUT
            }

            val write = plugpag.writeToNFCCard(cardData)

            if (write.result != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao escrever no cartão")
            }

            "Escrita realizada com sucesso"
        }
    }


    /**
     * ação de autenticação, controlada pela pps
     */
    fun authPagBank() {
        performAction {
            val cardData = PlugPagNFCAuth(
                PlugPagNearFieldCardData.ONLY_M,
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A
            )

            val auth = plugpag.authNFCCardDirectly(cardData, TIMEOUT)

            if (auth != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao autenticar")
            }
            "Autenticado com sucesso"
        }
    }

    /**
     * Função de Incremento do valor escrito no cartão
     */
    fun incrementPagBank() {

        performAction {

            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.serialNumber == null) {
                throw Exception("Cartão não identificado")
            }

            val authData = PlugPagNFCAuthDirectly(
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A,
                detect.serialNumber!!
            )

            val auth = plugpag.justAuthNfcDirectly(authData)

            if (auth != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao autenticar")
            }

            plugpag.incrementNfcValue(DEMO_SLOT.toByte(), VALUE_100_LE)

            "Incrementado"
        }

    }

    /**
     * Função de Decremento do valor escrito no cartão
     */
    fun decrementPagBank() {

        performAction {

            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.serialNumber == null) {
                throw Exception("Cartão não identificado")
            }

            val authData = PlugPagNFCAuthDirectly(
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A,
                detect.serialNumber!!
            )

            val auth = plugpag.justAuthNfcDirectly(authData)

            if (auth != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao autenticar")
            }

            plugpag.decrementNfcValue(DEMO_SLOT.toByte(), VALUE_50_LE)

            "Decrementado"
        }
    }

    /**
     * Função para mover os dados entre blocos
     */
    fun restoreAndTransferPagBank() {

        performAction {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.serialNumber == null) {
                throw Exception("Cartão não identificado")
            }

            val authData = PlugPagNFCAuthDirectly(
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A,
                detect.serialNumber!!
            )

            val auth = plugpag.justAuthNfcDirectly(authData)

            if (auth != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao autenticar")
            }

            plugpag.restoreAndTransferNfcValue(DEMO_SLOT.toByte(), DEMO_SLOT_DEST.toByte())

            "Transferido"
        }
    }


    /**
     * Ler o valor do bloco
     * para confirmar se o valor foi transferido
     */
    fun readTransferedBlock() {
        performAction {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result != PlugPag.NFC_RET_OK || detect.serialNumber == null) {
                throw Exception("Cartão não identificado")
            }

            val authData = PlugPagNFCAuthDirectly(
                DEMO_SLOT_DEST.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_A,
                detect.serialNumber!!
            )

            val auth = plugpag.justAuthNfcDirectly(authData)

            if (auth != PlugPag.NFC_RET_OK) {
                throw Exception("Falha ao autenticar")
            }

            val cardData = PlugPagSimpleNFCData(
                EM1KeyType.TYPE_A.ordinal,
                DEMO_SLOT_DEST,
                ByteArray(16)
            )

            val readData = plugpag.readNFCCardDirectly(cardData)

            if (readData.result != PlugPag.NFC_RET_OK || readData.slots[readData.startSlot][KEY_DATA] == null) {
                throw Exception("Falha ao ler o cartão")
            }

            readData.toStringFormatted()
        }
    }

    @Synchronized
    fun performAction(
        action: suspend () -> String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _state.postValue(NFCState.PROCESSING)
                startDirectly()
                val result = action.invoke()
                _eventTextMessage.postValue(result)
            } catch (e: Exception) {
                _eventTextMessage.postValue(e.message)
            } finally {
                stopDirectly()
                _state.postValue(NFCState.IDLE)
            }
        }
    }
}
