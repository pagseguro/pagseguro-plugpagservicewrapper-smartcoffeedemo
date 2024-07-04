package br.com.uol.pagbank.plugpagservice.demo.ui.payment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.uol.pagbank.plugpagservice.demo.R
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment

class PaymentInstallmentAdapter(
    context: Context
): RecyclerView.Adapter<PaymentInstallmentAdapter.ViewHolder>() {
    private var inflater: LayoutInflater = LayoutInflater.from(context)

    private var data: List<PlugPagInstallment> = listOf()
    private var clickListener: ItemClickListener? = null

    fun setData(newData: List<PlugPagInstallment>?) {
        this.data = newData ?: listOf()
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(
            R.layout.fragment_payment_installment,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(
            data[position],
            clickListener
        )
    }

    override fun getItemCount() = data.size

    fun getItem(id: Int) = data[id]

    fun setClickListener(itemClickListener: ItemClickListener?) {
        this.clickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, ppInstallment: PlugPagInstallment?, position: Int)
    }

    class ViewHolder internal constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var plugPagInstallment: PlugPagInstallment? = null
        private var clickListener: ItemClickListener? = null

        private var tvInstallmentAmount: TextView = itemView.findViewById<TextView>(R.id.tvInstallmentAmount)
        private var tvInstallmentValue: TextView = itemView.findViewById<TextView>(R.id.tvInstallmentValue)

        init {
            itemView.setOnClickListener(this)
        }

        fun setData(plugPagInstallment: PlugPagInstallment, clickListener: ItemClickListener?) {
            this.plugPagInstallment = plugPagInstallment
            this.clickListener = clickListener

            this.tvInstallmentAmount.text = "${plugPagInstallment.quantity}"
            this.tvInstallmentValue.text = "%.2f".format(plugPagInstallment.amount / 100f)
        }

        override fun onClick(view: View) {
            clickListener?.onItemClick(view, plugPagInstallment, adapterPosition)
        }
    }
}
