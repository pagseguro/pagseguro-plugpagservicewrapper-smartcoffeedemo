package br.com.uol.pagbank.plugpagservice.demo.ui.nfc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.com.uol.pagbank.plugpagservice.demo.databinding.FragmentNfcBinding
import br.com.uol.pagbank.plugpagservice.demo.extensions.getIn
import br.com.uol.pagbank.plugpagservice.demo.extensions.getOut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NFCFragment : Fragment() {
    private var _binding: FragmentNfcBinding? = null
    private val binding get() = _binding!!

    private lateinit var nfcViewModel: NFCViewModel

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
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
        val textView: TextView = binding.tvNFCStatus
//        nfcViewModel.eventText.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        nfcViewModel.eventTextResource.observe(viewLifecycleOwner) {
            textView.text = requireContext().resources.getText(it)
        }

        binding.btnDetect.setOnClickListener { onClickDetect(it) }
        buttons.add(binding.btnDetect)
        
        binding.btnRemove.setOnClickListener { onClickRemove(it) }
        buttons.add(binding.btnRemove)

        binding.btnRead.setOnClickListener { onClickRead(it) }
        buttons.add(binding.btnRead)

        binding.btnReadLot.setOnClickListener { onClickReadLot(it) }
        buttons.add(binding.btnReadLot)

        binding.btnReadDirectly.setOnClickListener { onClickReadDirectly(it) }
        buttons.add(binding.btnReadDirectly)

        binding.btnWrite.setOnClickListener { onClickWrite(it) }
        buttons.add(binding.btnWrite)

        binding.btnWriteLot.setOnClickListener { onClickWriteLot(it) }
        buttons.add(binding.btnWriteLot)

        binding.btnWriteDirectly.setOnClickListener { onClickWriteDirectly(it) }
        buttons.add(binding.btnWriteDirectly)

        binding.btnAuth.setOnClickListener { onClickAuth(it) }
        buttons.add(binding.btnAuth)

        binding.btnAuthDirectly.setOnClickListener { onClickAuthDirectly(it) }
        buttons.add(binding.btnAuthDirectly)

        binding.btnAbort.setOnClickListener { onClickAbort(it) }
        // buttons.add(binding.btnAbort)
    }

    fun onClickDetect(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.detectAction() },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickRemove(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.removeAction() },
            {
                enableUI()
                showMessage(it?.let { if (it) "Removed" else "Present" } ?: "Error")
            }
        )
    }

    fun onClickRead(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.readAction() },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    private val sector = 4

    fun onClickReadLot(view: View?) {
        doOperation(
            { disableUI() },
            // slot 2/3 está com a senha quebrada
            { nfcViewModel.readActionLot(sector) },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickReadDirectly(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.readDirectlyAction() },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickWrite(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.writeAction() },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickWriteLot(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.writeActionLot(sector) },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickWriteDirectly(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.writeDirectlyAction() },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickAuth(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.authAction() },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickAuthDirectly(view: View?) {
        doOperation(
            { disableUI() },
            { nfcViewModel.authDirectlyAction() },
            {
                enableUI()
                showMessage(it?:"Error")
            }
        )
    }

    fun onClickAbort(view: View?) {
        doOperation { nfcViewModel.abortAction() }
    }

    private fun disableUI() {
        buttons.forEach { it.isEnabled = false }
        binding.viwLoading.getIn()
        // binding.viwLoading.visibility = View.VISIBLE
    }

    private fun enableUI() {
        buttons.forEach { it.isEnabled = true }
        binding.viwLoading.getOut()
        // binding.viwLoading.visibility = View.GONE
    }

    private fun showMessage(message: String) {
        binding.tvNFCStatus.text = message
    }

    private fun <R> doOperation(back: () -> R) = doOperation({}, back, {})
    private fun <R> doOperation(pre: () -> Unit?, back: () -> R, pos: (r: R) -> Unit) {
        pre()
        coroutineScope.launch {
            val r = back()
            CoroutineScope(Dispatchers.Main).launch {
                pos(r)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
