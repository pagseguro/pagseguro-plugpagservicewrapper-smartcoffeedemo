package br.com.uol.pagbank.plugpagservice.demo.ui.nfc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.com.uol.pagbank.plugpagservice.demo.databinding.FragmentNfcBinding
import br.com.uol.pagbank.plugpagservice.demo.extensions.getIn
import br.com.uol.pagbank.plugpagservice.demo.extensions.getOut

class NFCFragment : Fragment() {
    private var _binding: FragmentNfcBinding? = null
    private val binding get() = _binding!!

    private lateinit var nfcViewModel: NFCViewModel
    private val buttons = mutableListOf<Button>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nfcViewModel = ViewModelProvider(this).get(NFCViewModel::class.java)
        _binding = FragmentNfcBinding.inflate(inflater, container, false)
        val root: View = binding.root

        configViews()

        return root
    }

    private fun configViews() {

        nfcViewModel.eventTextResource.observe(viewLifecycleOwner, ::showMessage)
        binding.btnDetect.setOnClickListener { nfcViewModel.detectDirectly() }
        binding.btnRemove.setOnClickListener { nfcViewModel.removeDirectly() }
        binding.btnRead.setOnClickListener { nfcViewModel.readPagBank() }
        binding.btnReadDirectly.setOnClickListener { nfcViewModel.readDirectly() }
        binding.btnWrite.setOnClickListener { nfcViewModel.writePagBank() }
        binding.btnWriteDirectly.setOnClickListener { nfcViewModel.writeDirectly() }
        binding.btnAuth.setOnClickListener { nfcViewModel.authPagBank() }
        binding.btnAuthDirectly.setOnClickListener { nfcViewModel.authDirectly() }
        binding.ilLoading.btnAbort.setOnClickListener { nfcViewModel.abort() }
        binding.btnIncrement.setOnClickListener { nfcViewModel.incrementPagBank() }
        binding.btnDecrement.setOnClickListener { nfcViewModel.decrementPagBank() }
        binding.btnRestore.setOnClickListener { nfcViewModel.restoreAndTransferPagBank() }
        binding.btnReadTransferred.setOnClickListener { nfcViewModel.readTransferedBlock() }

        nfcViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                NFCState.IDLE -> {
                    enableUI()
                }

                NFCState.PROCESSING -> {
                    disableUI()
                }
            }
        }
    }

    private fun disableUI() {
        buttons.forEach { it.isEnabled = false }
        binding.ilLoading.viwLoading.getIn()
    }

    private fun enableUI() {
        buttons.forEach { it.isEnabled = true }
        binding.ilLoading.viwLoading.getOut()
    }

    private fun showMessage(message: String) {
        binding.tvNFCStatus.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
