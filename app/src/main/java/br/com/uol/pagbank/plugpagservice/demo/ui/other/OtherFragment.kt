package br.com.uol.pagbank.plugpagservice.demo.ui.other

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.com.uol.pagbank.plugpagservice.demo.databinding.FragmentOtherBinding

class OtherFragment : Fragment() {
    private var _binding: FragmentOtherBinding? = null
    private val binding get() = _binding!!

    private lateinit var otherViewModel: OtherViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        otherViewModel = ViewModelProvider(this)[OtherViewModel::class.java]
        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        val root: View = binding.root

        configViews()

        return root
    }

    private fun configViews() {
        val textView: TextView = binding.tvOther
        otherViewModel.eventText.observe(viewLifecycleOwner) {
            textView.text = it
        }
        otherViewModel.eventTextResource.observe(viewLifecycleOwner) {
            textView.text = requireContext().getText(it)
        }
        otherViewModel.eventModelText.observe(viewLifecycleOwner) {
            binding.tvModel.text = it
        }
        otherViewModel.eventSerialNumber.observe(viewLifecycleOwner) {
            binding.tvSerialNumber.text = it
        }
        binding.btnOtherReboot.setOnClickListener { otherViewModel.reboot() }
        binding.btnOtherBeep.setOnClickListener { otherViewModel.beep() }
        binding.btnOtherLed.setOnClickListener { otherViewModel.led() }
        binding.btnOtherLastTransaction.setOnClickListener { otherViewModel.lastTransaction() }
        binding.btnOtherReprintEstablishmentReceipt.setOnClickListener { otherViewModel.reprintEstablishmentReceipt() }
        binding.btnOtherReprintCustomerReceipt.setOnClickListener { otherViewModel.reprintCustomerReceipt() }
        binding.btnOtherUndoLastTransaction.setOnClickListener { otherViewModel.undoLastTransaction() }
        binding.btnSetStyle.setOnClickListener { otherViewModel.setStyleData() }
        binding.btnPrint.setOnClickListener {
            otherViewModel.print(
                context?.getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS
                )?.absolutePath ?: ""
            )
        }
        binding.btnSetDeeplink.setOnClickListener {
            otherViewModel.setDeepLink()
        }
        binding.btnRemoveDeeplink.setOnClickListener {
            otherViewModel.removeDeeplink()
        }
        binding.btnOpenAppStore.setOnClickListener {
            startActivity(otherViewModel.createInstallIntent())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
