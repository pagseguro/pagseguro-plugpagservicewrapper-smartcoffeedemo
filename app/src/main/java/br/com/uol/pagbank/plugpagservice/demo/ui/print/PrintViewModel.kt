package br.com.uol.pagbank.plugpagservice.demo.ui.print

import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class PrintViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }
    private val assets: AssetManager by lazy { KoinPlatformTools.defaultContext().get().get<AssetManager>() }

    companion object {
        private const val TWO_SECONDS_DELAY = 2000L
        private const val FILE_NAME = "teste.jpg"
    }

    // atualiza a mensagem exibida na tela
    private val _eventTextResource = MutableLiveData<Int>().apply {
        value = R.string.waiting
    }
    val eventTextResource: LiveData<Int> = _eventTextResource

    // atualiza a mensagem exibida na tela
    private val _eventText = MutableLiveData<String>().apply {
        value = ""
    }
    val eventText: LiveData<String> = _eventText

    // solicita permissões de escrita
    private val _needWritePermission = MutableLiveData<Boolean>().apply {
        value = false
    }
    val needWritePermission: LiveData<Boolean> = _needWritePermission

    init {
        resetMessage()
    }

    private fun resetMessage() {
        viewModelScope.launch {
            _eventTextResource.value = R.string.waiting
        }
    }

    private fun setMessage(message: Int) {
        viewModelScope.launch {
            _eventTextResource.value = message
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

    private fun copyFile(inSt: InputStream?, outSt: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int? = null
        while (inSt?.read(buffer).also { read = it!! } != -1) {
            read?.let { outSt.write(buffer, 0, it) }
        }
    }

    fun hasNeedWritePermission(has: Boolean) {
        _needWritePermission.value = !has
    }

    fun print(externalDir: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_needWritePermission.value == false) {
                viewModelScope.launch {
                    _needWritePermission.value = true
                }
                return@launch
            }

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
                    // executa a impressão do arquivo informado
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
}
