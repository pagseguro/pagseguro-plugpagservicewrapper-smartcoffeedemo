package br.com.uol.pagbank.plugpagservice.demo.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import br.com.uol.pagbank.plugpagservice.demo.model.State
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var paymentViewModel: PaymentViewModel

    private var installmentsAdapter: PaymentInstallmentAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        paymentViewModel = ViewModelProvider(this).get(PaymentViewModel::class.java)
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        configViews()

        return root
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

        paymentViewModel.state.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                State.GETTING_AMOUNT -> {
                    hideType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    hideResult()
                }
                State.GETTING_TYPE -> {
                    showType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    hideResult()
                }
                State.GETTING_INSTALLMENT_TYPE -> {
                    hideType()
                    showInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    hideResult()
                }
                State.GETTING_INSTALLMENTS -> {
                    hideType()
                    hideInstallmentType()
                    showInstallmentAmount()
                    hidePayment()
                    hideResult()
                }
                State.PAYING -> {
                    hideType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    showPayment()
                    hideResult()

                    paymentViewModel.doPay()
                }
                State.RESULT -> {
                    hideType()
                    hideInstallmentType()
                    hideInstallmentAmount()
                    hidePayment()
                    showResult()
                }
            }
        }

        paymentViewModel.eventText.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.tvPayment.text = it
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
            if (it == PaymentError.INVALID_AMOUNT) {
                binding.tvInput.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
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

        binding.btnDebit.setOnClickListener { paymentViewModel.setType(PaymentType.DEBIT) }
        binding.btnCredit.setOnClickListener { paymentViewModel.setType(PaymentType.CREDIT) }
        binding.btnVoucher.setOnClickListener { paymentViewModel.setType(PaymentType.VOUCHER) }
        binding.btnPix.setOnClickListener { paymentViewModel.setType(PaymentType.PIX) }
        binding.btnCancelType.setOnClickListener { paymentViewModel.resetState() }

        binding.btnSingle.setOnClickListener { paymentViewModel.setInstallmentType(InstallmentType.A_VISTA) }
        binding.btnVendedor.setOnClickListener { paymentViewModel.setInstallmentType(InstallmentType.PARC_VENDEDOR) }
        binding.btnComprador.setOnClickListener { paymentViewModel.setInstallmentType(InstallmentType.PARC_COMPRADOR) }
        binding.btnCancelType.setOnClickListener { paymentViewModel.resetState() }

        // binding.rclInstallmentAmount.setOnClickListener { paymentViewModel.setInstallmentsAmount(1) }
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
            binding.btnDebit.getIn()
            binding.btnCredit.getIn()
            binding.btnVoucher.getIn()
            binding.btnPix.getIn()
            binding.btnCancelType.getIn()
        }
    }

    private fun hideType() {
        if (binding.clType.visibility != View.GONE) {
            binding.clType.fadeOut()

            binding.bgType.fadeOut()
            binding.tvType.getOut()
            binding.btnDebit.getOut()
            binding.btnCredit.getOut()
            binding.btnVoucher.getOut()
            binding.btnPix.getOut()
            binding.btnCancelType.getOut()
        }
    }

    private fun showInstallmentType() {
        if (binding.clInstallmentType.visibility != View.VISIBLE) {
            binding.clInstallmentType.fadeIn()

            binding.bgInstallmentType.fadeIn()
            binding.tvInstallmentType.getIn()
            binding.btnSingle.getIn()
            binding.btnVendedor.getIn()
            binding.btnComprador.getIn()
            binding.btnCancelInstallmentType.getIn()
        }
    }

    private fun hideInstallmentType() {
        if (binding.clInstallmentType.visibility != View.GONE) {
            binding.clInstallmentType.fadeOut()

            binding.bgInstallmentType.fadeOut()
            binding.tvInstallmentType.getOut()
            binding.btnSingle.getOut()
            binding.btnVendedor.getOut()
            binding.btnComprador.getOut()
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

            binding.tvPayment.text = context?.getText(R.string.processing) ?: ""
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
//            binding.btnTryAgainDebit.getIn()
//            binding.btnTryAgainCredit.getIn()
//            binding.btnTryAgainVoucher.getIn()
//            binding.btnTryAgain.getIn()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
