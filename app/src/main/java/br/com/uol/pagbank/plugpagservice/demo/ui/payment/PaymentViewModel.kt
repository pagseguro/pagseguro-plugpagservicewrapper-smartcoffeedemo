package br.com.uol.pagbank.plugpagservice.demo.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.model.InstallmentType
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentError
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentType
import br.com.uol.pagbank.plugpagservice.demo.model.State
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

class PaymentViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }

    private var checkRequirementsCalled = false

    // Show error in flow
    private val _error = MutableLiveData<PaymentError>().apply {
        value = null
    }
    val error: LiveData<PaymentError> = _error

    // Control information flow
    private val _state = MutableLiveData<State>().apply {
        value = State.GETTING_AMOUNT
    }
    val state: LiveData<State> = _state

    // Payment amount
    private var _amount = 0
    private val _amountText = MutableLiveData<String>().apply {
        value = ""
    }
    val amountText: LiveData<String> = _amountText

    // Payment type
    private var _paymentType = PaymentType.DEBIT
    val paymentType: PaymentType = _paymentType

    // Installment type
    private var _installmentType = InstallmentType.A_VISTA

    // Installment amount
    private var _installmentAmount = 1

    // Show error in flow
    private val _installments = MutableLiveData<List<PlugPagInstallment>>().apply {
        value = mutableListOf()
    }
    val installments: LiveData<List<PlugPagInstallment>> = _installments

    // atualiza a mensagem exibida na tela
    private val _eventText = MutableLiveData<String>().apply {
        value = ""
    }
    val eventText: LiveData<String> = _eventText

    // Payment result
    private var _result = MutableLiveData<PlugPagTransactionResult>().apply {
        value = null
    }
    val result: LiveData<PlugPagTransactionResult> = _result

    init {
        resetState()
        updateText()
    }

    fun resetState() {
        clearErrors()
        _state.value = State.GETTING_AMOUNT
    }

    private fun clearErrors() {
        viewModelScope.launch {
            _error.value = null
        }
    }

    private fun addError(error: PaymentError) {
        viewModelScope.launch {
            _error.value = error
        }
    }

    fun checkRequirements() {
        if (checkRequirementsCalled) return
        checkRequirementsCalled = true

        viewModelScope.launch(Dispatchers.Default) {
            // verifica se o serviço de pagamento está autenticado
            if (!plugpag.isAuthenticated()) {
                // inicia o onboarding para autenticar o serviço
                plugpag.startOnBoarding()
            }
        }
    }

    fun enterNumber(entry: Int) {
        val initialAmount = _amount
        _amount *= 10
        _amount += entry
        if (_amount > MAX_AMOUNT)
            _amount = initialAmount
        else
            updateText()
    }

    fun back() {
        _amount /= 10

        updateText()
    }

    fun clear() {
        _amount = 0

        updateText()
    }

    fun setAmount() {
        if (_amount < MIN_AMOUNT) {
            addError(PaymentError.INVALID_AMOUNT)
            return
        }

        _state.value = State.GETTING_TYPE
    }

    fun setType(type: PaymentType) {
        _paymentType = type

        when (_paymentType) {
            PaymentType.DEBIT,
            PaymentType.VOUCHER,
            PaymentType.PIX -> {
                _installmentType = InstallmentType.A_VISTA
                _installmentAmount = 1
                _state.value = State.PAYING
            }

            PaymentType.CREDIT -> {
                if (_amount < MIN_PARC_AMOUNT) {
                    _installmentType = InstallmentType.A_VISTA
                    _installmentAmount = 1
                    _state.value = State.PAYING
                } else {
                    _state.value = State.GETTING_INSTALLMENT_TYPE
                }
            }
        }
    }

    fun setInstallmentType(type: InstallmentType) {
        _installmentType = type

        when (_installmentType) {
            InstallmentType.A_VISTA -> {
                _installmentAmount = 1
                _state.value = State.PAYING
            }
            InstallmentType.PARC_VENDEDOR,
            InstallmentType.PARC_COMPRADOR-> {
                refreshInstallments()

                _state.value = State.GETTING_INSTALLMENTS
            }
        }
    }

    private fun refreshInstallments() {
        _installments.value = listOf()
        viewModelScope.launch(Dispatchers.Default) {
            // calcula as parcelas disponíveis para o valor informado
            // retorna uma lista vazia se o valor for inválido (abaixo de 10 reais ou alto demais)
            var installments = plugpag.calculateInstallments("$_amount", _installmentType.value)
            if (installments.isEmpty()) {
                addError(PaymentError.INVALID_INSTALLMENTS)
                return@launch
            }
            // adiciona a opção de pagamento à vista, como primeira opção
            installments = installments.toMutableList().apply {
                add(0, PlugPagInstallment(1, _amount, _amount))
            }

            viewModelScope.launch {
                _installments.value = installments
            }
        }
    }

    fun setInstallmentsAmount(amount: Int) {
        if (amount < 1) {
            addError(PaymentError.INVALID_INSTALLMENTS)
            return
        }

        _installmentAmount = amount
        _state.value = State.PAYING
    }

    fun doPay() {
        if (_state.value != State.PAYING) {
            addError(PaymentError.INVALID_AMOUNT)
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            // verifica se o serviço de pagamento está autenticado
            if (!plugpag.isAuthenticated()) {
                // inicia o onboarding para autenticar o serviço
                plugpag.startOnBoarding()
                addError(PaymentError.INVALID_STATE)
                return@launch
            }

            // recebe os eventos de mensagens do serviço para instruir as ações do usuário
            plugpag.setEventListener(object : PlugPagEventListener {
                override fun onEvent(data: PlugPagEventData) {
                    data.customMessage?.let {
                        _eventText.value = it
                    }
                }
            })

            //configura o popup de impressão da via do cliente
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

            // efetivamente solicita ao serivço que realize o pagamento
            // o resultado da transação é retornado para posterior tratativa
            val result = plugpag.doPayment(
                PlugPagPaymentData(
                    _paymentType.value,
                    _amount,
                    _installmentType.value,
                    _installmentAmount,
                    "Teste",
                    true
                )
            )

            // passa o resultado da transação para a UI
            // os resultados mais comuns encontrados no result.errorCode são, mas não apenas:
            //  # "0000"
            //      para pagamentos aprovados
            //  - "C13" ou "B018"
            //      para pagamentos cancelados pelo usuário
            //  - "R 05" ou "R 14" ou "R 51" ou "R 57" ou "R 59" ou "R 62" ou "R 63" ou "R 65" ou "R 75" ou "R 78" ou "R 82" ou "R 91" ou "B024" ou "M3011"
            //      para pagamentos recusados (não autorizados)
            //  - "R 55"
            //      para pagamentos recusados por senha incorreta
            //  - "A050" ou "A306" ou "A307" ou "A019" ou "B028" ou "A011" ou "A053"
            //      para pagamentos com erro de comunicação
            //  - "C40" ou "C43"
            //      se houver um erro interno na leitura do cartão
            //  - "C60" ou "C61"
            //      se houver um erro na leitura do cartão, pedindo para o usuário tentar novamente
            //  - "C70" ou "C84"
            //      se o tipo do cartão inserido for diferente do tipo selecionado
            //  - "C83" ou "C87"
            //      se o cartão aproximado for inválido, sugerindo usar o chip
            //  - "M831"
            //      se a venda por aproximação não for autorizada
            //  - "S20"f
            //      se o pagamento for duplicado
            //  - "C12"
            //      se o tempo esperando o cartão acabar
            //  - "B059"
            //      se o tempo esperando o qr code acabar
            //  - "S46"
            //      se o cartão tiver muitas tentativas
            //  - "SV03"
            //      se o serviço de pagamento estiver ocupado
            viewModelScope.launch {
                _result.value = result
                _state.value = State.RESULT
            }
        }
    }

    fun tryAgain() {
        _state.value = State.PAYING
    }

    fun abort() {
        if (_state.value != State.PAYING) {
            addError(PaymentError.INVALID_STATE)
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            // interrompe a transação atual
            plugpag.abort()
        }
    }

    private fun updateText() {
        var text = "%.2f".format(_amount / 100f)
        if (text.length >= 7) {
            text = text.substring(0, text.length - 6) + "." + text.substring(text.length - 6)
        }
        _amountText.value = text
    }

    companion object {
        const val MIN_AMOUNT = 1_00
        const val MIN_PARC_AMOUNT = 10_00
        const val MAX_AMOUNT = 250_000_00
    }
}
