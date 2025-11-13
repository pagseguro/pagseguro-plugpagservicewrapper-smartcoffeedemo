package br.com.uol.pagbank.plugpagservice.demo.ui.preauto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagbank.plugpagservice.demo.databinding.FragmentPreAutoBinding
import br.com.uol.pagbank.plugpagservice.demo.extensions.getIn
import br.com.uol.pagbank.plugpagservice.demo.extensions.getOut
import br.com.uol.pagbank.plugpagservice.demo.model.InstallmentType


class PreAutoFragment : Fragment() {

    private var _binding: FragmentPreAutoBinding? = null

    private val binding get() = _binding!!

    private lateinit var preAutoViewModel: PreAutoViewModel

    private var preAutoItemAdapter: PreAutoItemAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preAutoViewModel = ViewModelProvider(this)[PreAutoViewModel::class.java]
        _binding = FragmentPreAutoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        configViews()

        return root
    }

    private fun configViews() {

        preAutoViewModel.eventText.observe(viewLifecycleOwner) {
            binding.tvPreAuto.text = it
        }
        binding.rclPreAutoItems.setLayoutManager(LinearLayoutManager(context))
        binding.rclPreAutoItems.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        preAutoItemAdapter = PreAutoItemAdapter(
            requireContext(),
            { result ->
                preAutoViewModel.doPreAutoCancel(
                    result.transactionId!!,
                    result.transactionCode!!
                )
            },
            { result ->
                preAutoViewModel.doPreAutoEffectuate(
                    result.amount?.toInt() ?: 0,
                    result.transactionId,
                    result.transactionCode
                )
            })
        binding.rclPreAutoItems.setAdapter(preAutoItemAdapter)

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf(
                ItemSpinner(
                    requireContext().getString(R.string.single),
                    InstallmentType.A_VISTA
                ),
                ItemSpinner(
                    requireContext().getString(R.string.seller),
                    InstallmentType.PARC_VENDEDOR
                ),
                ItemSpinner(
                    requireContext().getString(R.string.buyer),
                    InstallmentType.PARC_COMPRADOR
                )
            )
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ilCardData.spInstallmentType.adapter = spinnerAdapter

        binding.btnClose.setOnClickListener {
            preAutoViewModel.backToIdle()
        }
        binding.btnConsulting.setOnClickListener {
            preAutoViewModel.getPreAutoData()
        }
        binding.btnConsultingKeyed.setOnClickListener {
            preAutoViewModel.showKeyed()
        }
        binding.ilCardData.btnContinueToPayment.setOnClickListener {
            val selectedItem = binding.ilCardData.spInstallmentType.selectedItem as ItemSpinner
            preAutoViewModel.getPreAutoDataKeyed(
                binding.ilCardData.etValue.text.toString().toInt(),
                binding.ilCardData.etTransactionDate.text.toString(),
                binding.ilCardData.etCv.text.toString(),
                selectedItem.type.value,
                binding.ilCardData.etNumberOfInstallments.text.toString().toInt(),
                binding.ilCardData.etCardNumber.text.toString(),
                binding.ilCardData.etCvv.text.toString(),
                binding.ilCardData.etExpire.text.toString(),
            )
        }

        binding.ilLoading.btnAbort.setOnClickListener {
            preAutoViewModel.abort()
        }

        preAutoViewModel.results.observe(viewLifecycleOwner) { list ->
            preAutoItemAdapter?.setData(list.orEmpty())
        }

        preAutoViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                PreAutoState.IDLE -> {
                    binding.ilLoading.viwLoading.getOut()
                    binding.clPreAutoItems.getOut()
                    binding.ilCardData.clPreAutoKeyed.getOut()
                    binding.ilCardData.etCv.getOut()
                    binding.ilCardData.etTransactionDate.getOut()
                    binding.ilCardData.etValue.getOut()
                    binding.ilCardData.spInstallmentType.getOut()
                    binding.ilCardData.etNumberOfInstallments.getOut()
                    binding.clButtons.getIn()
                }

                PreAutoState.LISTING -> {
                    binding.ilLoading.viwLoading.getOut()
                    binding.clButtons.getOut()
                    binding.ilCardData.clPreAutoKeyed.getOut()
                    binding.ilCardData.etCv.getOut()
                    binding.ilCardData.etTransactionDate.getOut()
                    binding.ilCardData.etValue.getOut()
                    binding.ilCardData.spInstallmentType.getOut()
                    binding.ilCardData.etNumberOfInstallments.getOut()
                    binding.clPreAutoItems.getIn()
                }

                PreAutoState.PROCESSING -> {
                    binding.clPreAutoItems.getOut()
                    binding.clButtons.getOut()
                    binding.ilLoading.btnAbort.getOut()
                    binding.ilCardData.clPreAutoKeyed.getOut()
                    binding.ilCardData.etCv.getOut()
                    binding.ilCardData.etTransactionDate.getOut()
                    binding.ilCardData.etValue.getOut()
                    binding.ilCardData.spInstallmentType.getOut()
                    binding.ilCardData.etNumberOfInstallments.getOut()
                    binding.ilLoading.viwLoading.getIn()
                }

                PreAutoState.CONSULTING -> {
                    binding.clPreAutoItems.getOut()
                    binding.clButtons.getOut()
                    binding.ilCardData.clPreAutoKeyed.getOut()
                    binding.ilCardData.etCv.getOut()
                    binding.ilCardData.etTransactionDate.getOut()
                    binding.ilCardData.etValue.getOut()
                    binding.ilCardData.spInstallmentType.getOut()
                    binding.ilCardData.etNumberOfInstallments.getOut()
                    binding.ilLoading.btnAbort.getIn()
                    binding.ilLoading.viwLoading.getIn()
                }

                PreAutoState.CONSULTING_KEYED -> {
                    clearTexts()
                    binding.clPreAutoItems.getOut()
                    binding.clButtons.getOut()
                    binding.ilCardData.spInstallmentType.getIn()
                    binding.ilCardData.etNumberOfInstallments.getIn()
                    binding.ilCardData.clPreAutoKeyed.getIn()
                    binding.ilCardData.etCv.getIn()
                    binding.ilCardData.etTransactionDate.getIn()
                    binding.ilCardData.etValue.getIn()
                }
            }
        }

        preAutoViewModel.error.observe(viewLifecycleOwner) { error ->

            when (error) {
                PreAutoError.EMPTY_VALUE_ERROR -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.empty_value,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun clearTexts() {
        binding.ilCardData.etCardNumber.text?.clear()
        binding.ilCardData.etCvv.text?.clear()
        binding.ilCardData.etExpire.text?.clear()
        binding.ilCardData.etTransactionDate.text?.clear()
        binding.ilCardData.etCv.text?.clear()
        binding.ilCardData.etValue.text?.clear()
        binding.ilCardData.etNumberOfInstallments.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
