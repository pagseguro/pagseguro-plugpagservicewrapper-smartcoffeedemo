package br.com.uol.pagbank.plugpagservice.demo.ui.payment

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagbank.plugpagservice.demo.databinding.FragmentPaymentBinding
import br.com.uol.pagbank.plugpagservice.demo.extensions.fadeIn
import br.com.uol.pagbank.plugpagservice.demo.extensions.fadeOut
import br.com.uol.pagbank.plugpagservice.demo.extensions.getIn
import br.com.uol.pagbank.plugpagservice.demo.extensions.getOut
import br.com.uol.pagbank.plugpagservice.demo.model.InstallmentType
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentError
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentType
import br.com.uol.pagbank.plugpagservice.demo.model.PaymentState
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment
import java.util.Locale

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private var paymentState: PaymentState = PaymentState.GETTING_AMOUNT

    private lateinit var paymentViewModel: PaymentViewModel

    private var installmentsAdapter: PaymentInstallmentAdapter? = null
    private var typeAdapter: PaymentTypeAdapter? = null
    private var installmentTypeAdapter: PaymentTypeAdapter? = null

    private val processing = context?.getText(R.string.processing) ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        paymentViewModel = ViewModelProvider(this)[PaymentViewModel::class.java]
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        configInitialAmount()

        configViews()

        return root
    }

    private fun configInitialAmount() {
        /**
         * O Launcher PagBank Emite esses campos ao utilizar um teclado fisico de um terminal
         * com esses campos você pode abrir o App ja com os valores inseridos
         * o valor vem em centavos
         *
         * E necessario o intent-filter especificado no AndroidManifest
         */
        val intent = requireActivity().intent
        val keyedValue = intent.getIntExtra("value", 0)
        val event = intent.getStringExtra("event")

        paymentViewModel.setInitialAmount(keyedValue)
    }

    override fun onResume() {
        super.onResume()

        paymentViewModel.checkRequirements()
    }

    private fun configViews() {
        paymentViewModel.amountText.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.tvInput.text = it
        }

        paymentViewModel.paymentState.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            paymentState = it
            when (it) {
                PaymentState.GETTING_AMOUNT -> {
                    hideType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    hideResult()
                    hidePreautoKeyed()
                }

                PaymentState.GETTING_TYPE -> {
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    hideResult()
                    hidePreautoKeyed()
                    showType()
                }

                PaymentState.GETTING_INSTALLMENT_TYPE -> {
                    hideType()
                    hideInstallmentAmount()
                    hidePayment()
                    hideResult()
                    hidePreautoKeyed()
                    showInstallmentType()
                }

                PaymentState.GETTING_INSTALLMENTS -> {
                    hideType()
                    hideInstallmentType()
                    hidePayment()
                    hideResult()
                    hidePreautoKeyed()
                    showInstallmentAmount()
                }

                PaymentState.GETTING_CARD_DATA -> {
                    hideType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    hideResult()
                    clearTexts()
                    showPreautoKeyed()
                }

                PaymentState.PAYING -> {
                    hideType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hideResult()
                    hidePreautoKeyed()
                    showPayment()
                    paymentViewModel.doPay()
                }

                PaymentState.RESULT -> {
                    hideType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    hidePreautoKeyed()
                    showResult()
                }
            }
        }

        paymentViewModel.eventText.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.tvPayment.text = it
            if (processing == it.lowercase(Locale.ROOT) || it.isEmpty()) {
                binding.btnCancelPayment.visibility = View.INVISIBLE
            } else {
                binding.btnCancelPayment.visibility = View.VISIBLE
            }
        }

        paymentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            if (it.errorCode == "0000") {
                binding.tvResult.text = requireContext().getText(R.string.success)
            } else {
                binding.tvResult.text = "${it.errorCode}\n${it.message}"
            }

            when (it.errorCode) {
                "0000", "S20" -> {
                    binding.btnTryAgainCredit.getOut()
                    binding.btnTryAgainDebit.getOut()
                    binding.btnTryAgainVoucher.getOut()
                    binding.btnTryAgain.getOut()
                }

                "C70", "C84" -> {
                    when (paymentViewModel.paymentType) {
                        PaymentType.DEBIT -> {
                            binding.btnTryAgainDebit.getOut()
                            binding.btnTryAgainCredit.getIn()
                            binding.btnTryAgainVoucher.getIn()
                        }

                        PaymentType.CREDIT -> {
                            binding.btnTryAgainDebit.getIn()
                            binding.btnTryAgainCredit.getOut()
                            binding.btnTryAgainVoucher.getIn()
                        }

                        PaymentType.VOUCHER -> {
                            binding.btnTryAgainDebit.getIn()
                            binding.btnTryAgainCredit.getIn()
                            binding.btnTryAgainVoucher.getOut()
                        }

                        else -> {
                            binding.btnTryAgainDebit.getOut()
                            binding.btnTryAgainCredit.getOut()
                            binding.btnTryAgainVoucher.getOut()
                        }
                    }

                    binding.btnTryAgain.getIn()
                }

                else -> {
                    binding.btnTryAgainDebit.getOut()
                    binding.btnTryAgainCredit.getOut()
                    binding.btnTryAgainVoucher.getOut()
                    binding.btnTryAgain.getIn()
                }
            }
        }

        binding.rclType.setLayoutManager(LinearLayoutManager(context))
        typeAdapter = PaymentTypeAdapter(requireContext()).apply {
            setClickListener(
                object : PaymentTypeAdapter.ItemClickListener {
                    override fun onItemClick(
                        view: View?,
                        paymentType: PaymentTypeAdapter.PlugPagType?
                    ) {
                        paymentType?.let {
                            paymentViewModel.setType(it.type as PaymentType)
                        }
                    }
                }
            )
        }.apply {
            setData(
                listOf(
                    PaymentTypeAdapter.PlugPagType(
                        1,
                        requireContext().getString(R.string.debit),
                        PaymentType.DEBIT
                    ),
                    PaymentTypeAdapter.PlugPagType(
                        2,
                        requireContext().getString(R.string.credit),
                        PaymentType.CREDIT
                    ),
                    PaymentTypeAdapter.PlugPagType(
                        3,
                        requireContext().getString(R.string.voucher),
                        PaymentType.VOUCHER
                    ),
                    PaymentTypeAdapter.PlugPagType(
                        4,
                        requireContext().getString(R.string.pix),
                        PaymentType.PIX
                    ),
                    PaymentTypeAdapter.PlugPagType(
                        5,
                        requireContext().getString(R.string.pre_auto),
                        PaymentType.PRE_AUTO_CARD
                    ),
                    PaymentTypeAdapter.PlugPagType(
                        6,
                        requireContext().getString(R.string.pre_auto_keyed),
                        PaymentType.PRE_AUTO_KEYED
                    )
                )
            )
        }
        binding.rclType.setAdapter(typeAdapter)

        binding.ilPreAutoKeyed.btnContinueToPayment.setOnClickListener {
            paymentViewModel.setCardData(
                binding.ilPreAutoKeyed.etCardNumber.text.toString(),
                binding.ilPreAutoKeyed.etExpire.text.toString(),
                binding.ilPreAutoKeyed.etCvv.text.toString(),
            )
        }

        binding.rclInstallmentType.setLayoutManager(LinearLayoutManager(context))
        installmentTypeAdapter = PaymentTypeAdapter(requireContext()).apply {
            setClickListener(
                object : PaymentTypeAdapter.ItemClickListener {
                    override fun onItemClick(
                        view: View?,
                        paymentType: PaymentTypeAdapter.PlugPagType?
                    ) {
                        paymentType?.let {
                            paymentViewModel.setInstallmentType(it.type as InstallmentType)
                        }
                    }
                }
            )
        }.apply {
            setData(
                listOf(
                    PaymentTypeAdapter.PlugPagType(
                        1,
                        requireContext().getString(R.string.single),
                        InstallmentType.A_VISTA
                    ),
                    PaymentTypeAdapter.PlugPagType(
                        2,
                        requireContext().getString(R.string.seller),
                        InstallmentType.PARC_VENDEDOR
                    ),
                    PaymentTypeAdapter.PlugPagType(
                        3,
                        requireContext().getString(R.string.buyer),
                        InstallmentType.PARC_COMPRADOR
                    )
                )
            )
        }
        binding.rclInstallmentType.setAdapter(installmentTypeAdapter)

        binding.rclInstallmentAmount.setLayoutManager(LinearLayoutManager(context))
        installmentsAdapter = PaymentInstallmentAdapter(requireContext()).apply {
            setClickListener(
                object : PaymentInstallmentAdapter.ItemClickListener {
                    override fun onItemClick(
                        view: View?,
                        ppInstallment: PlugPagInstallment?,
                        position: Int
                    ) {
                        ppInstallment?.let {
                            paymentViewModel.setInstallmentsAmount(it.quantity)
                        }
                    }
                }
            )
        }
        binding.rclInstallmentAmount.setAdapter(installmentsAdapter)
        paymentViewModel.installments.observe(viewLifecycleOwner) {
            installmentsAdapter?.setData(it)
        }

        paymentViewModel.error.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                PaymentError.INVALID_AMOUNT -> {
                    binding.tvInput.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            R.anim.shake
                        )
                    )
                }

                PaymentError.INVALID_STATE -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.not_authenticated,
                        Toast.LENGTH_LONG
                    ).show()
                }

                PaymentError.INVALID_SETUP -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.no_pps_found,
                        Toast.LENGTH_LONG
                    ).show()
                }

                PaymentError.EMPTY_VALUE -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.empty_value,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    // não executa nenhuma ação
                }
            }
            paymentViewModel.resetState()
        }

        binding.btnZero.setOnClickListener { paymentViewModel.enterNumber(0) }
        binding.btnOne.setOnClickListener { paymentViewModel.enterNumber(1) }
        binding.btnTwo.setOnClickListener { paymentViewModel.enterNumber(2) }
        binding.btnThree.setOnClickListener { paymentViewModel.enterNumber(3) }
        binding.btnFour.setOnClickListener { paymentViewModel.enterNumber(4) }
        binding.btnFive.setOnClickListener { paymentViewModel.enterNumber(5) }
        binding.btnSix.setOnClickListener { paymentViewModel.enterNumber(6) }
        binding.btnSeven.setOnClickListener { paymentViewModel.enterNumber(7) }
        binding.btnEight.setOnClickListener { paymentViewModel.enterNumber(8) }
        binding.btnNine.setOnClickListener { paymentViewModel.enterNumber(9) }
        binding.btnBackspace.setOnClickListener { paymentViewModel.back() }
        binding.btnAllClear.setOnClickListener { paymentViewModel.clear() }
        binding.btnPay.setOnClickListener { paymentViewModel.setAmount() }

        binding.btnCancelType.setOnClickListener { paymentViewModel.resetState() }

        binding.btnCancelInstallmentAmount.setOnClickListener { paymentViewModel.resetState() }

        binding.btnCancelPayment.setOnClickListener { paymentViewModel.abort() }

        binding.btnTryAgainDebit.setOnClickListener { paymentViewModel.setType(PaymentType.DEBIT) }
        binding.btnTryAgainCredit.setOnClickListener { paymentViewModel.setType(PaymentType.CREDIT) }
        binding.btnTryAgainVoucher.setOnClickListener { paymentViewModel.setType(PaymentType.VOUCHER) }
        binding.btnTryAgain.setOnClickListener { paymentViewModel.tryAgain() }
        binding.btnBack.setOnClickListener {
            paymentViewModel.clear()
            paymentViewModel.resetState()
        }
    }

    private fun showType() {
        if (binding.clType.visibility != View.VISIBLE) {
            binding.clType.fadeIn()

            binding.bgType.fadeIn()
            binding.tvType.getIn()
            binding.rclType.getIn()
            binding.btnCancelType.getIn()
        }
    }

    private fun hideType() {
        if (binding.clType.visibility != View.GONE) {
            binding.clType.fadeOut()

            binding.bgType.fadeOut()
            binding.tvType.getOut()
            binding.rclType.getOut()
            binding.btnCancelType.getOut()
        }
    }

    private fun showInstallmentType() {
        if (binding.clInstallmentType.visibility != View.VISIBLE) {
            binding.clInstallmentType.fadeIn()

            binding.bgInstallmentType.fadeIn()
            binding.tvInstallmentType.getIn()
            binding.rclInstallmentType.getIn()
            binding.btnCancelInstallmentType.getIn()
        }
    }

    private fun hideInstallmentType() {
        if (binding.clInstallmentType.visibility != View.GONE) {
            binding.clInstallmentType.fadeOut()

            binding.bgInstallmentType.fadeOut()
            binding.tvInstallmentType.getOut()
            binding.rclInstallmentType.getOut()
            binding.btnCancelInstallmentType.getOut()
        }
    }

    private fun showInstallmentAmount() {
        if (binding.clInstallmentAmount.visibility != View.VISIBLE) {
            binding.clInstallmentAmount.fadeIn()

            binding.bgInstallmentAmount.fadeIn()
            binding.tvInstallmentAmount.getIn()
            binding.rclInstallmentAmount.getIn()
            binding.btnCancelInstallmentAmount.getIn()
        }
    }

    private fun hideInstallmentAmount() {
        if (binding.clInstallmentAmount.visibility != View.GONE) {
            binding.clInstallmentAmount.fadeOut()

            binding.bgInstallmentAmount.fadeOut()
            binding.tvInstallmentAmount.getOut()
            binding.rclInstallmentAmount.getOut()
            binding.btnCancelInstallmentAmount.getOut()
        }
    }

    private fun showPayment() {
        if (binding.clPayment.visibility != View.VISIBLE) {
            binding.clPayment.fadeIn()

            binding.bgPayment.fadeIn()
            binding.tvPayment.getIn()
            binding.btnCancelPayment.getIn()

            binding.tvPayment.text = processing
            binding.btnCancelPayment.visibility = View.INVISIBLE
        }
    }

    private fun hidePayment() {
        if (binding.clPayment.visibility != View.GONE) {
            binding.clPayment.fadeOut()

            binding.bgPayment.fadeOut()
            binding.tvPayment.getOut()
            binding.btnCancelPayment.getOut()
        }
    }

    private fun showResult() {
        if (binding.clResult.visibility != View.VISIBLE) {
            binding.clResult.fadeIn()

            binding.bgResult.fadeIn()
            binding.tvResult.getIn()
            binding.btnBack.getIn()
        }
    }

    private fun hideResult() {
        if (binding.clResult.visibility != View.GONE) {
            binding.clResult.fadeOut()

            binding.bgResult.fadeOut()
            binding.tvResult.getOut()
            binding.btnTryAgainDebit.getOut()
            binding.btnTryAgainCredit.getOut()
            binding.btnTryAgainVoucher.getOut()
            binding.btnTryAgain.getOut()
            binding.btnBack.getOut()
        }
    }

    private fun showPreautoKeyed() {
        if (binding.ilPreAutoKeyed.clPreAutoKeyed.visibility != View.VISIBLE) {
            binding.ilPreAutoKeyed.clPreAutoKeyed.fadeIn()
        }
    }

    private fun hidePreautoKeyed() {
        if (binding.ilPreAutoKeyed.clPreAutoKeyed.visibility != View.GONE) {
            binding.ilPreAutoKeyed.clPreAutoKeyed.fadeOut()
        }
    }

    private fun clearTexts() {
        binding.ilPreAutoKeyed.etCardNumber.text?.clear()
        binding.ilPreAutoKeyed.etExpire.text?.clear()
        binding.ilPreAutoKeyed.etCvv.text?.clear()
    }

    fun onKeyPressed(keyCode: Int): Boolean {
        when (paymentState) {
            PaymentState.GETTING_AMOUNT -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_0 -> paymentViewModel.enterNumber(0)
                    KeyEvent.KEYCODE_1 -> paymentViewModel.enterNumber(1)
                    KeyEvent.KEYCODE_2 -> paymentViewModel.enterNumber(2)
                    KeyEvent.KEYCODE_3 -> paymentViewModel.enterNumber(3)
                    KeyEvent.KEYCODE_4 -> paymentViewModel.enterNumber(4)
                    KeyEvent.KEYCODE_5 -> paymentViewModel.enterNumber(5)
                    KeyEvent.KEYCODE_6 -> paymentViewModel.enterNumber(6)
                    KeyEvent.KEYCODE_7 -> paymentViewModel.enterNumber(7)
                    KeyEvent.KEYCODE_8 -> paymentViewModel.enterNumber(8)
                    KeyEvent.KEYCODE_9 -> paymentViewModel.enterNumber(9)

                    KeyEvent.KEYCODE_DEL,
                    KeyEvent.KEYCODE_BACKSLASH -> paymentViewModel.back()

                    KeyEvent.KEYCODE_BACK -> paymentViewModel.clear()
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_NUMPAD_ENTER -> paymentViewModel.setAmount()

                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_UP -> return true

                    else -> return false
                }
            }

            PaymentState.GETTING_TYPE -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_1 -> paymentViewModel.setType(PaymentType.DEBIT)
                    KeyEvent.KEYCODE_2 -> paymentViewModel.setType(PaymentType.CREDIT)
                    KeyEvent.KEYCODE_3 -> paymentViewModel.setType(PaymentType.VOUCHER)
                    KeyEvent.KEYCODE_4 -> paymentViewModel.setType(PaymentType.PIX)
                    KeyEvent.KEYCODE_5 -> paymentViewModel.setType(PaymentType.PRE_AUTO_CARD)

                    KeyEvent.KEYCODE_DEL,
                    KeyEvent.KEYCODE_BACKSLASH,
                    KeyEvent.KEYCODE_BACK -> binding.btnCancelType.performClick()

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // Do nothing.
                    }

                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // Do nothing.
                    }

                    else -> return false
                }
            }

            PaymentState.GETTING_INSTALLMENT_TYPE -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_1 -> paymentViewModel.setInstallmentType(InstallmentType.A_VISTA)
                    KeyEvent.KEYCODE_2 -> paymentViewModel.setInstallmentType(InstallmentType.PARC_VENDEDOR)
                    KeyEvent.KEYCODE_3 -> paymentViewModel.setInstallmentType(InstallmentType.PARC_COMPRADOR)

                    KeyEvent.KEYCODE_DEL,
                    KeyEvent.KEYCODE_BACKSLASH,
                    KeyEvent.KEYCODE_BACK -> binding.btnCancelInstallmentType.performClick()

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // Do nothing.
                    }

                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // Do nothing.
                    }

                    else -> return false
                }
            }

            PaymentState.GETTING_INSTALLMENTS -> {
                when (keyCode) {
                    KeyEvent.KEYCODE_0,
                    KeyEvent.KEYCODE_1,
                    KeyEvent.KEYCODE_2,
                    KeyEvent.KEYCODE_3,
                    KeyEvent.KEYCODE_4,
                    KeyEvent.KEYCODE_5,
                    KeyEvent.KEYCODE_6,
                    KeyEvent.KEYCODE_7,
                    KeyEvent.KEYCODE_8,
                    KeyEvent.KEYCODE_9 -> paymentViewModel.setInstallmentsAmount(keyCode - KeyEvent.KEYCODE_0)

                    KeyEvent.KEYCODE_DEL,
                    KeyEvent.KEYCODE_BACKSLASH,
                    KeyEvent.KEYCODE_BACK -> binding.btnCancelInstallmentAmount.performClick()

                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // Do nothing.
                    }

                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // Do nothing.
                    }

                    else -> return false
                }
            }

            else -> {}
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
