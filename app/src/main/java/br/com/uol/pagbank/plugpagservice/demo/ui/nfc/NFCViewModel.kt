package br.com.uol.pagbank.plugpagservice.demo.ui.nfc

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.EM1KeyType
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuth
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuthDirectly
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagSimpleNFCData
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools
import java.lang.Exception
import java.nio.charset.Charset

class NFCViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }

    companion object {
        private const val TIMEOUT = 10 // seconds
        private const val DEMO_SLOT = 18 // index

        private const val TWO_SECONDS_DELAY = 2000L

        private const val MODEL_SK800 = "SK800"
        private const val KEY_DATA = "data"
        private const val KEY_PASS = "pwd"

        private val charsetUTF8 = Charset.forName("UTF-8")
        private val DEFAULT_KEY_NFC = byteArrayOf(
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte()
        )

        // len = 16
        private val demoText = arrayOf(
            "demo_text    01 ".toByteArray(charsetUTF8),
            "demo_text  02   ".toByteArray(charsetUTF8),
            "demo_text     03".toByteArray(charsetUTF8)
        )
    }

    // Event message resource
    private val _eventTextResource = MutableLiveData<Int>().apply {
        value = R.string.waiting
    }
    val eventTextResource: LiveData<Int> = _eventTextResource

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

    // liga a antena NFC e ativa o cartão
    private fun startDirectly(): Boolean {
        return try {
            val start = plugpag.startNFCCardDirectly()

            if (start != PlugPag.NFC_RET_OK) {
                stopDirectly()
                false
            } else
                true
        } catch (e: PlugPagException) {
            stopDirectly()
            false
        }
    }

    // desliga a antena NFC e desativa o cartão
    private fun stopDirectly(): Boolean {
        return try {
            val stop = plugpag.stopNFCCardDirectly()
            return (stop == PlugPag.NFC_RET_OK)
        } catch (e: PlugPagException) {
            false
        }
    }

    // aborta o processo de detecção
    private fun abort(): Boolean {
        return try {
            val abort = plugpag.abortNFC()

            if (abort.result != PlugPag.NFC_RET_OK) {
                stopDirectly()
                false
            } else
                true
        } catch (e: PlugPagException) {
            false
        }
    }

    // detecta se há um cartão próximo à antena, se houver retorna seu serial
    private fun detectDirectly(): ByteArray? {
        return try {
            val detect = plugpag.detectNfcCardDirectly(
                PlugPagNearFieldCardData.ONLY_M,
                TIMEOUT
            )

            if (detect.result == PlugPag.NFC_RET_OK && detect.serialNumber != null) {
                detect.serialNumber!!
            } else {
                stopDirectly()
                null
            }
        } catch (e: Exception) {
            stopDirectly()
            null
        }
    }

    // detecta se há um cartão próximo à antena, se houver retorna seu serial
    private fun detectDirectlyString() = detectDirectly()?.toString(charsetUTF8)

    // detecta se há um cartão próximo à antena, se houver retorna seu serial
    private fun removeDirectly(): Boolean? {
        return try {
            val detect = plugpag.detectNfcCardDirectly(PlugPagNearFieldCardData.ONLY_M, 0)

            if (detect.result == PlugPag.NFC_RET_OK && detect.serialNumber != null)
                false
            else {
                stopDirectly()
                true
            }
        } catch (e: Exception) {
            stopDirectly()
            if (e.message != null && e.message!!.contains("No Near Field Card found"))
                true
            else
                null
        }
    }

    // autentica o cartão, é necessário que a antena já esteja ligada e o cartão ativado
    private fun authDirectly(cardSerial: ByteArray): Boolean {
        return try {
            val cardData = PlugPagNFCAuthDirectly(
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_B,
                cardSerial
            )

            val auth = plugpag.justAuthNfcDirectly(cardData)

            return if (auth != PlugPag.NFC_RET_OK) {
                stopDirectly()
                false
            } else {
                true
            }
        } catch (e: PlugPagException) {
            stopDirectly()
            false
        }
    }

    // lê o cartão, é necessário que a antena já esteja ligada e o cartão ativado
    private fun readDirectly(): ByteArray? {
        return try {
            val cardData = PlugPagSimpleNFCData(
                EM1KeyType.TYPE_A.ordinal,
                DEMO_SLOT,
                ByteArray(16)
            )

            val read = plugpag.readNFCCardDirectly(cardData)

            return if (read.result == PlugPag.NFC_RET_OK && read.slots[read.startSlot][KEY_DATA] != null)
                read.slots[read.startSlot][KEY_DATA]
            else {
                stopDirectly()
                null
            }
        } catch (e: PlugPagException) {
            stopDirectly()
            null
        }
    }

    // lê o cartão, é necessário que a antena já esteja ligada e o cartão ativado
    private fun readDirectlyString() = readDirectly()?.toString(charsetUTF8)

    // escreve no cartão, é necessário que a antena já esteja ligada e o cartão ativado
    private fun writeDirectly(): Boolean {
        return try {
            val cardData = PlugPagSimpleNFCData(
                EM1KeyType.TYPE_B.ordinal,
                DEMO_SLOT,
                demoText[0]
            )

            val write = plugpag.writeToNFCCardDirectly(cardData)

            if (write != PlugPag.NFC_RET_OK) {
                stopDirectly()
                false
            } else
                true
        } catch (e: PlugPagException) {
            stopDirectly()
            false
        }
    }

    // ação de abort
    fun abortAction() = abort()

    // ação de detecção
    fun detectAction(): String? {
        return try {
            if (Build.MODEL != MODEL_SK800)
                if (!startDirectly())
                    return null

            val cardSerial = detectDirectlyString()
            if (cardSerial == null)
                return null

            if (Build.MODEL != MODEL_SK800)
                stopDirectly()

            cardSerial
        } catch (e: PlugPagException) {
            plugpag.stopNFCCardDirectly()
            e.message
        }
    }

    // ação de remoção
    fun removeAction() = removeDirectly()

    // ação de leitura, controlada pela pps
    fun readAction(): String? {
        return try {
            val cardData = PlugPagNearFieldCardData().apply {
                startSlot = DEMO_SLOT
                endSlot = DEMO_SLOT
                slots[DEMO_SLOT][KEY_PASS] = DEFAULT_KEY_NFC
                timeOutRead = TIMEOUT
            }

            val read = plugpag.readFromNFCCard(cardData)

            return if (read.result == PlugPag.NFC_RET_OK) {
                var readed = ""

                for (slot in read.startSlot .. read.endSlot) {
                    read.slots[slot][KEY_DATA]?.let {
                        val content = it.toString(charsetUTF8)
                        readed += (if (readed.isNotEmpty()) "\n" else "") + slot + ":" + content
                    }
                }

                return readed
            } else
                null
        } catch (e: PlugPagException) {
            e.message
        }
    }

    // ação de leitura, controlada pela pps
    fun readActionLot(sector: Int): String? {
        return try {
            val cardData = PlugPagNearFieldCardData().apply {
                // lê um setor inteiro
                startSlot = sector * 4
                endSlot = sector * 4 + 3
                // identifica a senha para o setor
                for (slot in startSlot..endSlot) {
                    slots[slot][KEY_PASS] = DEFAULT_KEY_NFC
                }
                // tempo limite de leitura
                timeOutRead = TIMEOUT
            }

            // solicita a leitura do setor
            //  - detect
            //  - auth
            //  - read / write
            //  - close
            val read = plugpag.readFromNFCCard(cardData)

            // tratamentos de acordo com o resultado
            return if (read.result == PlugPag.NFC_RET_OK) {
                var readed = ""

                for (slot in read.startSlot .. read.endSlot) {
                    read.slots[slot][KEY_DATA]?.let {
                        val content = it.toString(charsetUTF8)
                        readed += (if (readed.isNotEmpty()) "\n" else "") + slot + ":" + content
                    }
                }

                return readed
            } else
                null
        } catch (e: PlugPagException) {
            e.message
        }
    }

    // ação de leitura, controlada pelo app
    fun readDirectlyAction(): String? {
        return try {
            if (Build.MODEL != MODEL_SK800)
                if (!startDirectly())
                    return null

            val cardSerial = detectDirectly()
            if (cardSerial == null)
                return null

            if (Build.MODEL == MODEL_SK800)
                if (!startDirectly())
                    return null

            if (!authDirectly(cardSerial))
                return null

            val read = readDirectlyString()
            if (read == null)
                return null

            stopDirectly()

            return read
        } catch (e: PlugPagException) {
            plugpag.stopNFCCardDirectly()
            e.message
        }
    }

    // ação de escrita, controlada pela pps
    fun writeAction(): String? {
        return try {
            val cardData = PlugPagNearFieldCardData().apply {
                startSlot = DEMO_SLOT
                endSlot = DEMO_SLOT
                slots[DEMO_SLOT][KEY_PASS] = DEFAULT_KEY_NFC
                slots[DEMO_SLOT][KEY_DATA] = demoText[0]
                timeOutRead = TIMEOUT
            }

            val write = plugpag.writeToNFCCard(cardData)

            return if (write.result == PlugPag.NFC_RET_OK) {
                "Success"
            } else
                null
        } catch (e: PlugPagException) {
            e.message
        }
    }

    // ação de escrita, controlada pela pps
    fun writeActionLot(sector: Int): String? {
        return try {
            val cardData = PlugPagNearFieldCardData().apply {
                // escreve num setor inteiro
                startSlot = sector * 4
                endSlot = sector * 4 + 3
                // conteudo e senha para o setor
                for (slot in startSlot..endSlot) {
                    slots[slot][KEY_PASS] = DEFAULT_KEY_NFC
                    slots[slot][KEY_DATA] = demoText[slot % demoText.size]
                }
                // tempo limite de escrita
                timeOutRead = TIMEOUT
            }

            // solicita a leitura do setor
            //  - detect
            //  - auth
            //  - read / write
            //  - close
            val write = plugpag.writeToNFCCard(cardData)

            // tratamentos de acordo com o resultado
            return if (write.result == PlugPag.NFC_RET_OK) {
                "Success"
            } else
                null
        } catch (e: PlugPagException) {
            e.message
        }
    }

    // ação de escrita, controlada pelo app
    fun writeDirectlyAction(): String? {
        return try {
            if (Build.MODEL != MODEL_SK800)
                if (!startDirectly())
                    return null

            val cardSerial = detectDirectly()
            if (cardSerial == null)
                return null

            if (Build.MODEL == MODEL_SK800)
                if (!startDirectly())
                    return null

            if (!authDirectly(cardSerial))
                return null

            val write = writeDirectly()
            if (!write)
                return null

            stopDirectly()

            "Success"
        } catch (e: PlugPagException) {
            plugpag.stopNFCCardDirectly()
            e.message
        }
    }

    // ação de autenticação, controlada pela pps
    fun authAction(): String? {
        return try {
            val cardData = PlugPagNFCAuth(
                PlugPagNearFieldCardData.ONLY_M,
                DEMO_SLOT.toByte(),
                DEFAULT_KEY_NFC,
                EM1KeyType.TYPE_B
            )

            if (Build.MODEL != MODEL_SK800)
                if (!startDirectly())
                    return null

            val auth = plugpag.authNFCCardDirectly(cardData, TIMEOUT)

            if (Build.MODEL != MODEL_SK800)
                stopDirectly()

            return if (auth == PlugPag.NFC_RET_OK)
                "Success"
            else
                null
        } catch (e: PlugPagException) {
            e.message
        }
    }

    // ação de autenticação, controlada pelo app
    fun authDirectlyAction(): String? {
        return try {
            if (Build.MODEL != MODEL_SK800)
                if (!startDirectly())
                    return null

            val cardSerial = detectDirectly()
            if (cardSerial == null)
                return null

            if (Build.MODEL == MODEL_SK800)
                if (!startDirectly())
                    return null

            if (!authDirectly(cardSerial))
                return null

            stopDirectly()

            "Success"
        } catch (e: PlugPagException) {
            plugpag.stopNFCCardDirectly()
            e.message
        }
    }
}
