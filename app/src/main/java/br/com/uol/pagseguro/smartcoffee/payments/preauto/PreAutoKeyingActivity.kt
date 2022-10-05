package br.com.uol.pagseguro.smartcoffee.payments.preauto

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import br.com.uol.pagseguro.smartcoffee.R
import br.com.uol.pagseguro.smartcoffee.demoInterno.CustomDialog
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED_CREATE
import br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants
import br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_NUMBER
import br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TOTAL_VALUE
import br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_DATA
import br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_OPERATION
import br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_A_VISTA
import kotlinx.android.synthetic.main.activity_pre_auto_keying.btn_cancel
import kotlinx.android.synthetic.main.activity_pre_auto_keying.btn_ok
import kotlinx.android.synthetic.main.activity_pre_auto_keying.tv_amount
import kotlinx.android.synthetic.main.activity_pre_auto_keying.tv_installments
import kotlinx.android.synthetic.main.activity_pre_auto_keying.txt_credit_card_cvv
import kotlinx.android.synthetic.main.activity_pre_auto_keying.txt_credit_card_exp_date
import kotlinx.android.synthetic.main.activity_pre_auto_keying.txt_pan
import kotlinx.android.synthetic.main.activity_pre_auto_keying.txt_transaction_code
import kotlinx.android.synthetic.main.activity_pre_auto_keying.txt_transaction_date
import java.text.DecimalFormat
import java.util.*

class PreAutoKeyingActivity : AppCompatActivity() {

    lateinit var dialog: CustomDialog
    var transactionType: Int = 1
    var installment: Int = 1
    lateinit var operationType: PreAutoOperation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_auto_keying)

        initView()

        val returnIntent = Intent()
        btn_ok.setOnClickListener {
            try {
                val preAutoPayment = createPreAutoData()

                returnIntent.putExtra(PREAUTO_DATA, preAutoPayment)
                returnIntent.putExtra(PREAUTO_OPERATION, operationType)

                setResult(RESULT_OK, returnIntent)
                finish()
            } catch (e: Exception) {
                showDialog(e.message.toString())
            }
        }
        btn_cancel.setOnClickListener {
            setResult(RESULT_CANCELED, returnIntent)
            finish()
        }
    }

    private fun createPreAutoData(): PreAutoPayment {
        val preAutoPayment: PreAutoPayment
        if (operationType == PREAUTO_KEYED || operationType == PREAUTO_KEYED_CREATE) {
            preAutoPayment = PreAutoPayment(
                installment,
                validateInput(txt_pan),
                formatExpDate(),
                validateInput(txt_credit_card_cvv),
                "",
                ""
            )
        } else {
            preAutoPayment = PreAutoPayment(
                installment,
                validateInput(txt_pan),
                formatExpDate(),
                validateInput(txt_credit_card_cvv),
                validateInput(txt_transaction_code),
                formatTransactionDate()
            )
        }
        return preAutoPayment
    }


    private fun showDialog(message: String) {
        if (!dialog.isShowing) {
            dialog.show()
        }
        dialog.setMessage(message)
    }

    private fun validateInput(input: EditText): String {
        val text = input.text.toString()
        if (text.isEmpty()) {
            throw Exception("Preencha os campos necessários")
        }
        return text
    }

    private fun formatExpDate(): String {
        var expDate = txt_credit_card_exp_date.text.toString()
        if (expDate.length == 4) {
            expDate = expDate.substring(2) + expDate.substring(0, 2)
        }
        return expDate
    }

    private fun formatTransactionDate(): String {
        var date = txt_transaction_date.text.toString()
        if (date.length == 8) {
            val day = date.substring(0, 2)
            val month = date.substring(2, 4)
            val year = date.substring(4, 8)
            date = "$year-$month-$day"
        } else {
            throw Exception("Digite a data no formato DDMMAAAA")
        }
        return date
    }

    private fun getTotalAmount(): String {
        val convertedValue: Double = intent.extras.getInt(TOTAL_VALUE).toDouble() / 100
        val currencyLocale = DecimalFormat.getCurrencyInstance(Locale("pt", "BR"))
        return currencyLocale.format(convertedValue)
            .replace(
                currencyLocale
                    .currency.currencyCode, currencyLocale.currency.currencyCode + ""
            )
    }

    private fun initView() {
        tv_amount.text = getTotalAmount()
        dialog = CustomDialog(this)
        dialog.setOnCancelListener { dialogCancel: DialogInterface ->
            dialogCancel.dismiss()
        }
        transactionType = intent.extras.getInt(InstallmentConstants.TRANSACTION_TYPE, 1)
        installment = intent.extras.getInt(INSTALLMENT_NUMBER, 1)
        operationType = intent.extras.get(PREAUTO_OPERATION) as PreAutoOperation

        if (operationType == PREAUTO_KEYED_CREATE || operationType == PREAUTO_KEYED) {
            txt_transaction_date.visibility = View.GONE
            txt_transaction_code.visibility = View.GONE
            txt_transaction_date.isFocusable = false
            txt_transaction_code.isFocusable = false
            if (transactionType == INSTALLMENT_TYPE_A_VISTA) {
                tv_installments.text = getString(R.string.text_a_vista)
            } else {
                val installmentText = " " + installment.toString() + "X"
                tv_installments.text = getString(R.string.text_a_prazo) + installmentText
            }
        }
    }
}

