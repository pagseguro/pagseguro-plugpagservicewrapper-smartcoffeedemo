package br.com.uol.pagbank.plugpagservice.demo.ui.preauto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.extensions.formatExpDate
import br.com.uol.pagbank.plugpagservice.demo.extensions.formatTransactionDate
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEffectuatePreAutoData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoQueryData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

class PreAutoViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }

    private var _results = MutableLiveData<List<PlugPagTransactionResult>>().apply {
        value = emptyList()
    }
    val results: LiveData<List<PlugPagTransactionResult>> = _results

    private val _error = MutableLiveData<PreAutoError>().apply {
        value = null
    }
    val error: LiveData<PreAutoError> = _error

    /**
     *  Atualiza a mensagem exibida na tela
     */
    private val _eventText = MutableLiveData<String>().apply {
        value = "Aguardando"
    }
    val eventText: LiveData<String> = _eventText

    private var _state = MutableLiveData<PreAutoState>().apply {
        value = PreAutoState.IDLE
    }

    val state: LiveData<PreAutoState> = _state

    /**
     * Essa função é responsavel por pegar as pré-autorizadas no cartão detectado
     */
    fun getPreAutoData() {
        viewModelScope.launch(Dispatchers.IO) {

            emitState(PreAutoState.CONSULTING)

            /**
             * Recebe os eventos de mensagens do serviço para instruir as ações do usuário
             */
            plugpag.setEventListener(object : PlugPagEventListener {
                override fun onEvent(data: PlugPagEventData) {
                    data.customMessage.let {
                        _eventText.value = it
                    }
                }
            })

            val plugPagQueryResult = plugpag.getPreAutoList()

            if (plugPagQueryResult.result == PlugPag.RET_OK) {
                _results.postValue(plugPagQueryResult.transactions)
                _eventText.postValue("Lista Retornada")
                emitState(PreAutoState.LISTING)
            } else {
                _eventText.postValue("${plugPagQueryResult.errorCode} - ${plugPagQueryResult.message}")
                emitState(PreAutoState.IDLE)
            }
        }
    }

    /**
     * Essa função é responsavel por cancelar a pré-autorizada
     */
    fun doPreAutoCancel(transactionId: String, transactionCode: String) {
        viewModelScope.launch(Dispatchers.IO) {

            emitState(PreAutoState.PROCESSING)

            val plugPagTransactionResult =
                plugpag.doPreAutoCancel(transactionId, transactionCode)


            if (plugPagTransactionResult.result == PlugPag.RET_OK) {
                _eventText.postValue("Pré-autorizada Cancelada")
                emitState(PreAutoState.IDLE)
            } else {
                _eventText.postValue("${plugPagTransactionResult.errorCode} - ${plugPagTransactionResult.message}")
                emitState(PreAutoState.LISTING)
            }
        }
    }

    /**
     * Essa função é responsavel por efetivar a pré-autorizada selecionada
     */
    fun doPreAutoEffectuate(value: Int, transactionId: String?, transactionCode: String?) {
        viewModelScope.launch(Dispatchers.IO) {

            emitState(PreAutoState.PROCESSING)

            val data = PlugPagEffectuatePreAutoData(
                value,
                "Teste",
                true,
                transactionId,
                transactionCode
            )

            val plugPagTransactionResult = plugpag.doEffectuatePreAuto(data)
            if (plugPagTransactionResult.result == PlugPag.RET_OK) {
                _eventText.postValue("Pré-autorizada Efetuada")
                emitState(PreAutoState.IDLE)
            } else {
                _eventText.postValue("${plugPagTransactionResult.errorCode} - ${plugPagTransactionResult.message}")
                emitState(PreAutoState.LISTING)
            }
        }
    }

    /**
     *  Essa função é responsavel por pegar a pré-autorizada digitando
     *  as informações da comprovante
     *
     *  @param Amount = Valor da transação em centavos
     *  @param TransactionDate = Data da Transação
     *  @param TransactionCode = CV: Codigo que sai na nota
     *  @param InstallmentType = Tipo de parcelamento InstallmentType(Ex A_VISTA =  A_VISTA(PlugPag.INSTALLMENT_TYPE_A_VISTA))
     *  @param Installments = Numero de Parcelas A_VISTA = 1 , COMPRADOR e VENDEDOR podem varias os valores
     *  @param CardNumber = PAN -> Numero do cartão (0000 0000 0000 0000)
     *  @param CVV = Codigo de Segurança do cartão 3-4 digitos
     *  @param ExpirationDate = Data de validade do cartão
     */
    fun getPreAutoDataKeyed(
        amount: Int,
        transactionDate: String,
        transactionCode: String,
        installmentType: Int,
        installments: Int,
        cardNumber: String,
        cvv: String,
        expirationDate: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            if (cardNumber.isEmpty() ||
                expirationDate.isEmpty() ||
                cvv.isEmpty() ||
                expirationDate.isEmpty() ||
                transactionCode.isEmpty() ||
                transactionDate.isEmpty() ||
                amount == 0
            ) {
                addError(PreAutoError.EMPTY_VALUE_ERROR)
                return@launch
            }

            emitState(PreAutoState.CONSULTING)

            plugpag.setEventListener(object : PlugPagEventListener {
                override fun onEvent(data: PlugPagEventData) {
                    data.customMessage.let {
                        _eventText.value = it
                    }
                }
            })

            val plugPagQueryResult = plugpag.getPreAutoData(
                PlugPagPreAutoQueryData(
                    amount * 100,
                    installmentType,
                    installments,
                    cardNumber,
                    cvv,
                    expirationDate.formatExpDate(),
                    transactionDate.formatTransactionDate(),
                    transactionCode
                )
            )

            if (plugPagQueryResult.result == PlugPag.RET_OK) {
                _results.postValue(listOf(plugPagQueryResult))
                _eventText.postValue("Lista Retornada")
                emitState(PreAutoState.LISTING)
            } else {
                _eventText.postValue("${plugPagQueryResult.errorCode} - ${plugPagQueryResult.message}")
                emitState(PreAutoState.IDLE)
            }
        }
    }

    fun abort() {
        viewModelScope.launch(Dispatchers.IO) {
            plugpag.abort()
        }
    }

    /**
     * Volta pro estado base da tela
     */
    fun backToIdle() {
        emitState(PreAutoState.IDLE)
    }

    private fun emitState(newState: PreAutoState) {
        _state.postValue(newState)
    }

    fun showKeyed() {
        emitState(PreAutoState.CONSULTING_KEYED)
    }

    private fun addError(error: PreAutoError) {
        viewModelScope.launch {
            _error.value = error
        }
    }

}
