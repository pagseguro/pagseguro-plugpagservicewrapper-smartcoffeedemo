package br.com.uol.pagbank.plugpagservice.demo.ui.preauto

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagbank.plugpagservice.demo.databinding.FragmentPreautoItemBinding
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult

class PreAutoItemAdapter(
    context: Context,
    private val onCancel: (PlugPagTransactionResult) -> Unit,
    private val onEffectuate: (PlugPagTransactionResult) -> Unit
) : RecyclerView.Adapter<PreAutoItemAdapter.ViewHolder>() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    private var data: List<PlugPagTransactionResult> = emptyList()


    fun setData(newData: List<PlugPagTransactionResult>) {
        data = newData
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = FragmentPreautoItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onCancel, onEffectuate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(
            data[position]
        )
    }

    override fun getItemCount() = data.size

    class ViewHolder internal constructor(
        binding: FragmentPreautoItemBinding,
        private val onCancel: (PlugPagTransactionResult) -> Unit,
        private val onEffectuate: (PlugPagTransactionResult) -> Unit

    ) : RecyclerView.ViewHolder(binding.root) {
        private var tvCode = binding.tvTrasactionCode
        private var tvId = binding.tvTransactionId
        private var tvValue = binding.tvValue
        private var tvDateTime = binding.tvDateTime
        private var btnCancel = binding.btnCancel
        private var btnEffectuate = binding.btnEffectuate


        fun setData(result: PlugPagTransactionResult) {
            tvValue.text =
                itemView.context.getString(R.string.valor, result.amount?.toInt()?.div(100f))
            tvCode.text =
                itemView.context.getString(R.string.transaction_code, result.transactionCode)
            tvId.text = itemView.context.getString(R.string.transaction_id, result.transactionId)
            tvDateTime.text =
                itemView.context.getString(R.string.data_hora, result.date, result.time)
            btnCancel.setOnClickListener {
                onCancel(result)
            }
            btnEffectuate.setOnClickListener {
                onEffectuate(result)
            }
        }
    }
}
