package br.com.uol.pagbank.plugpagservice.demo.ui.print

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.com.uol.pagbank.plugpagservice.demo.databinding.FragmentPrintBinding

class PrintFragment : Fragment() {
    private var _binding: FragmentPrintBinding? = null
    private val binding get() = _binding!!

    private lateinit var printViewModel: PrintViewModel

    private val neededPermissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            "android.permission.READ_MEDIA_IMAGES",
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        printViewModel.hasNeedWritePermission(neededPermissions().isNotEmpty())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        printViewModel = ViewModelProvider(this).get(PrintViewModel::class.java)
        _binding = FragmentPrintBinding.inflate(inflater, container, false)
        val root: View = binding.root

        configViews()

        return root
    }

    private fun configViews() {
        val textView: TextView = binding.tvPrint
        printViewModel.eventText.observe(viewLifecycleOwner) {
            textView.text = it
        }
        printViewModel.eventTextResource.observe(viewLifecycleOwner) {
            textView.text = requireContext().getText(it)
        }
        printViewModel.hasNeedWritePermission(neededPermissions().isNotEmpty())
        printViewModel.needWritePermission.observe(viewLifecycleOwner) { request ->
            if (request) requestPermissions()
        }

        binding.btnPrintInternal.setOnClickListener {
            printViewModel.print(context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: "")
        }
    }

    private fun neededPermissions(): List<String> {
        return neededPermissionsList.filter {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        neededPermissions().takeIf { it.isNotEmpty() }?.run {
            permissionLauncher.launch(neededPermissionsList.toTypedArray())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
