package br.com.uol.pagbank.plugpagservice.demo.ui.payment

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.uol.pagbank.plugpagservice.demo.extensions.formatExpDate
import br.com.uol.pagbank.plugpagservice.demo.model.InstallmentType
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentError
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentType
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentState
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoKeyingData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

class PaymentViewModel : ViewModel() {
    private val plugpag: PlugPag by lazy { KoinPlatformTools.defaultContext().get().get<PlugPag>() }
    private val packageManager: PackageManager by lazy {
        KoinPlatformTools.defaultContext().get().get<Context>().packageManager
    }

    private var checkRequirementsCalled = false

    /**
     * Responsável por mostrar erros no fluxo
     */
    private val _error = MutableLiveData<PaymentError>().apply {
        value = null
    }
    val error: LiveData<PaymentError> = _error

    /**
     * Controle de estado do fluxo
     */
    private val _paymentState = MutableLiveData<PaymentState>().apply {
        value = PaymentState.GETTING_AMOUNT
    }
    val paymentState: LiveData<PaymentState> = _paymentState

    /**
     * Valor do pagamento
     */
    private var _amount = 0
    private val _amountText = MutableLiveData<String>().apply {
        value = ""
    }
    val amountText: LiveData<String> = _amountText

    /**
     * Tipo de pagamento
     */
    private var _paymentType = PaymentType.DEBIT
    val paymentType: PaymentType = _paymentType

    /**
     * Tipo de parcelamento
     */
    private var _installmentType = InstallmentType.A_VISTA

    /**
     * Quantidade de parcelas
     */
    private var _installmentAmount = 1

    /**
     * Valores das parcelas
     */
    private val _installments = MutableLiveData<List<PlugPagInstallment>>().apply {
        value = mutableListOf()
    }
    val installments: LiveData<List<PlugPagInstallment>> = _installments

    /**
     * Atualiza a mensagem exibida na tela
     */
    private val _eventText = MutableLiveData<String>().apply {
        value = ""
    }
    val eventText: LiveData<String> = _eventText

    /**
     * Resultado da transação
     */
    private var _result = MutableLiveData<PlugPagTransactionResult>().apply {
        value = null
    }
    val result: LiveData<PlugPagTransactionResult> = _result

    /**
     * Dados do cartão
     */
    private var _cardNumber = ""
    private var _expire = ""
    private var _cvv = ""

    /**
     * Estado do pagamento
     */
    private var executingPayment = false

    init {
        resetState()
        updateText()
    }

    fun resetState() {
        clearErrors()
        _paymentState.value = PaymentState.GETTING_AMOUNT
    }

    /**
     * Limpa estado de erros
     */
    private fun clearErrors() {
        viewModelScope.launch {
            _error.value = null
        }
    }

    /**
     * Adiciona estado de erro
     */
    private fun addError(error: PaymentError) {
        viewModelScope.launch {
            _error.value = error
        }
    }

    /**
     * Verifica os requerimentos
     */
    private fun hasRequirements(): Boolean {
        try {
            if (packageManager.getPackageInfo(PLUGPAG_SERVICE_PACKAGE_NAME, 0) == null) {
                addError(PaymentError.INVALID_SETUP)
                return false
            }
        } catch (ex: Exception) {
            addError(PaymentError.INVALID_SETUP)
            return false
        }

        /**
         * Verifica se o serviço de pagamento está autenticado
         */
        if (!plugpag.isAuthenticated()) {
            // Inicia o onboarding para autenticar o serviço
            plugpag.startOnBoarding()
            addError(PaymentError.INVALID_STATE)
            return false
        }

        return true
    }

    fun checkRequirements() {
        if (checkRequirementsCalled) return
        checkRequirementsCalled = true

        viewModelScope.launch(Dispatchers.Default) {
            if (!hasRequirements())
                checkRequirementsCalled = false
        }
    }

    /**
     * Funções do teclado
     */
    fun enterNumber(entry: Int) {
        viewModelScope.launch {
            val initialAmount = _amount
            _amount *= 10
            _amount += entry
            if (_amount > MAX_AMOUNT)
                _amount = initialAmount
            else
                updateText()
        }
    }

    fun back() {
        _amount /= 10

        updateText()
    }

    fun clear() {
        _amount = 0

        updateText()
    }


    /**
     * Função para atualizar o valor
     */
    fun setAmount() {
        /**
         * O valor mínimo para transação é de 1 real
         */
        if (_amount < MIN_AMOUNT) {
            addError(PaymentError.INVALID_AMOUNT)
            return
        }

        _paymentState.value = PaymentState.GETTING_TYPE
    }

    fun setInitialAmount(amount: Int) {
        _amount = amount
        updateText()
    }

    /**
     * Função para definir qual tipo de pagamento
     */
    fun setType(type: PaymentType) {
        _paymentType = type

        when (_paymentType) {
            PaymentType.DEBIT,
            PaymentType.VOUCHER,
            PaymentType.PIX -> {
                _installmentType = InstallmentType.A_VISTA
                _installmentAmount = 1
                _paymentState.value = PaymentState.PAYING
            }

            PaymentType.CREDIT, PaymentType.PRE_AUTO_CARD -> {
                if (_amount < MIN_PARC_AMOUNT) {
                    _installmentType = InstallmentType.A_VISTA
                    _installmentAmount = 1
                    _paymentState.value = PaymentState.PAYING
                } else {
                    _paymentState.value = PaymentState.GETTING_INSTALLMENT_TYPE
                }
            }

            PaymentType.PRE_AUTO_KEYED -> {
                if (_amount < MIN_PARC_AMOUNT) {
                    _installmentType = InstallmentType.A_VISTA
                    _installmentAmount = 1
                    _paymentState.value = PaymentState.GETTING_CARD_DATA
                } else {
                    _paymentState.value = PaymentState.GETTING_INSTALLMENT_TYPE
                }
            }
        }
    }

    /**
     * Função para definir a quantidade de parcelas ou se vai ser uma compra a vista
     */
    fun setInstallmentType(type: InstallmentType) {
        _installmentType = type

        when (_installmentType) {
            InstallmentType.A_VISTA -> {
                _installmentAmount = 1
                if (_paymentType.value != PaymentType.PRE_AUTO_KEYED.value) {
                    _paymentState.value = PaymentState.PAYING
                } else {
                    _paymentState.value = PaymentState.GETTING_CARD_DATA
                }
            }

            InstallmentType.PARC_VENDEDOR,
            InstallmentType.PARC_COMPRADOR -> {
                refreshInstallments()

                _paymentState.value = PaymentState.GETTING_INSTALLMENTS
            }
        }
    }

    private fun refreshInstallments() {
        _installments.value = listOf()
        viewModelScope.launch(Dispatchers.Default) {
            /**
             * Calcula as parcelas disponíveis para o valor informado
             */
            var installments = plugpag.calculateInstallments("$_amount", _installmentType.value)
            /**
             * Retorna uma lista vazia se o valor for inválido (abaixo de 10 reais ou alto demais)
             */
            if (installments.isEmpty()) {
                addError(PaymentError.INVALID_INSTALLMENTS)
                return@launch
            }
            /**
             * Adiciona a opção de pagamento à vista, como primeira opção
             */
            installments = installments.toMutableList().apply {
                add(0, PlugPagInstallment(1, _amount, _amount))
            }

            viewModelScope.launch {
                _installments.value = installments
            }
        }
    }

    /**
     * Função para definir a escolha da quantidade de parcelas
     */
    fun setInstallmentsAmount(amount: Int) {
        if (amount < 1) {
            addError(PaymentError.INVALID_INSTALLMENTS)
            return
        }
        _installmentAmount = amount
        if (_paymentType.value != PaymentType.PRE_AUTO_KEYED.value) {
            _paymentState.value = PaymentState.PAYING
        } else {
            _paymentState.value = PaymentState.GETTING_CARD_DATA
        }
    }

    /**
     * Função para coletar os dados do cartão na pré-autorizada digitada
     */
    fun setCardData(cardNumber: String, expirationDate: String, cvv: String) {

        if (cardNumber.isEmpty() || expirationDate.isEmpty() || cvv.isEmpty()) {
            addError(PaymentError.INVALID_SETUP)
            return
        }

        _cardNumber = cardNumber
        _expire = expirationDate.formatExpDate()
        _cvv = cvv
        _paymentState.postValue(PaymentState.PAYING)
    }

    /**
     * Função para realizar o pagamento
     */
    fun doPay() {
        if (_paymentState.value != PaymentState.PAYING) {
            addError(PaymentError.INVALID_STATE)
            return
        }

        /**
         * Operações de pagamento, ativações e outras operações são
         * BLOCANTES e não permitem outras operações em paralelo.
         * Caso seja chamado outra operação enquanto uma BLOCANTE esta rodando
         * sera retornado os Erros SV03 ou PP1047.
         * É Recomendado que o App trate as chamadas da PlugPag e evite as concorrência.
         * Além disso é possivel verificar
         * se a PPS esta ocupada através da função isServiceBusy do Wrapper
         */
        if (executingPayment) {
            return
        }
        executingPayment = true

        viewModelScope.launch(Dispatchers.Default) {
            if (!hasRequirements()) {
                executingPayment = false
                return@launch
            }

            /**
             * Recebe os eventos de mensagens do serviço para instruir as ações do usuário
             */
            plugpag.setEventListener(object : PlugPagEventListener {
                override fun onEvent(data: PlugPagEventData) {
                    when (data.eventCode) {
                        PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD -> {
                            _eventText.value += PASSWORD_CHAR
                        }

                        PlugPagEventData.EVENT_CODE_NO_PASSWORD -> {
                            _eventText.value = PASSWORD_HINT
                        }

                        else -> {
                            data.customMessage.let {
                                _eventText.value = it
                            }
                        }
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
             * Efetivamente solicita ao serivço que realize o pagamento
             * o resultado da transação é retornado para posterior tratativa
             */
            val result = when (_paymentType.value) {
                PaymentType.PRE_AUTO_CARD.value -> {
                    plugpag.doPreAutoCreate(
                        PlugPagPreAutoData(
                            _amount,
                            _installmentType.value,
                            _installmentAmount,
                            "Teste",
                            true
                        )
                    )
                }

                PaymentType.PRE_AUTO_KEYED.value -> {
                    plugpag.doPreAutoCreate(
                        PlugPagPreAutoKeyingData(
                            _amount,
                            _installmentType.value,
                            _installmentAmount,
                            "teste",
                            true,
                            _cardNumber,
                            _cvv,
                            _expire
                        )
                    )
                }

                else -> {
                    plugpag.doPayment(
                        PlugPagPaymentData(
                            _paymentType.value,
                            _amount,
                            _installmentType.value,
                            _installmentAmount,
                            "Teste",
                            true
                        )
                    )
                }
            }

            executingPayment = false

            /**
             * Trata os erros mais comuns encontrados no result.errorCode
             */
            when (result.errorCode) {
                "SV03", "PP1017" -> abort()
                // todo: tratar os erros pertinentes à aplicação
            }

            /** Passa o resultado da transação para a UI
             *   os resultados mais comuns encontrados no result.errorCode são, mas não apenas:
             *   # "0000"
             *      para pagamentos aprovados
             *  - "C13" ou "B018"
             *       para pagamentos cancelados pelo usuário
             *   - "R 05" ou "R 14" ou "R 51" ou "R 57" ou "R 59" ou "R 62" ou "R 63" ou "R 65" ou "R 75" ou "R 78" ou "R 82" ou "R 91" ou "B024" ou "M3011"
             *       para pagamentos recusados (não autorizados)
             *   - "R 55"
             *       para pagamentos recusados por senha incorreta
             *   - "A050" ou "A306" ou "A307" ou "A019" ou "B028" ou "A011" ou "A053"
             *       para pagamentos com erro de comunicação
             *   - "A012"
             *       para pagamentos com erros de comunicação durante resolução de nomes
             *       para resolver tente verificar a comunicação se esta OK e tente reiniciar o terminal
             *   - "C40"
             *       se houver um erro interno na leitura do cartão
             *   - "C43"
             *       se o cartão for removido antes de finalizar o fluxo de pagamento
             *   - "C60" ou "C61"
             *        se houver um erro na leitura do cartão, pedindo para o usuário tentar novamente
             *   - "C70" ou "C84"
             *       se o tipo do cartão inserido for diferente do tipo selecionado
             *       nesse caso verificar se selecionou o tipo de pagamento corretamente CREDITO ou DEBITO
             *       em caso de voucher verificar se esta credenciado corretamente
             *   - "C83" ou "C87"
             *       se o cartão aproximado for inválido, sugerindo usar o chip
             *   - "M831" ou "M815" ou "M826"
             *       se a venda não for autorizada por risco, tente via chip
             *   - "S20"
             *       se o pagamento for duplicado, ocorre quando tenta realizar uma venda com mesmo valor e cartão num curto intervalo de tempo
             *   - "C12"
             *       se o tempo para aproximar,inserir ou passar o cartão esgotar
             *   - "B059*    /      se o tempo para leitura do qr code esgotar
             *   - "S46"
             *       se o cartão tiver muitas tentativas
             *   - "SV03" ou "PP1047"
             *       se o serviço de pagamento estiver ocupado realizando outras operações
             *        nesse caso aguarde a operação acabar ou verifique o status usando isServiceBusy.
             */
            viewModelScope.launch {
                _result.value = result
                _paymentState.value = PaymentState.RESULT
            }
        }
    }

    fun tryAgain() {
        _paymentState.value = PaymentState.PAYING
    }

    /**
     *    Após executar o abort aguardar o retorno da função que você quer executar
     *    Ex: se doPay() esta rodando
     *    e chamamos abort() temos que esperar o resultado do doPay()
     *    Sempre esperar o resultado da outra operação mesmo tendo que chamar o abort()
     *    e necessário esperar a resposta antes de chamar o doPay() novamente
     */
    fun abort() {
        if (_paymentState.value != PaymentState.PAYING) {
            addError(PaymentError.INVALID_STATE)
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            /**
             * Interrompe a transação em andamento
             */
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
        const val PLUGPAG_SERVICE_PACKAGE_NAME = "br.com.uol.pagseguro.plugpagservice"
        const val PASSWORD_HINT = "Digite a senha\n"
        const val PASSWORD_CHAR = "*"
    }
}
