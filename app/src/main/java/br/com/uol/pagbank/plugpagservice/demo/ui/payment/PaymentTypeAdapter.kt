package br.com.uol.pagbank.plugpagservice.demo.ui.payment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.uol.pagbank.plugpagservice.demo.R
import java.io.Serializable

class PaymentTypeAdapter(
    context: Context
) : RecyclerView.Adapter<PaymentTypeAdapter.ViewHolder>() {
    private var inflater: LayoutInflater = LayoutInflater.from(context)

    private var data: List<PlugPagType> = listOf()
    private var clickListener: ItemClickListener? = null

    fun setData(newData: List<PlugPagType>?) {
        this.data = newData ?: listOf()
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(
            R.layout.fragment_payment_type_item,
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

    fun setClickListener(itemClickListener: ItemClickListener?) {
        this.clickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, paymentType: PlugPagType?)
    }

    class ViewHolder internal constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var plugPagType: PlugPagType? = null
        private var clickListener: ItemClickListener? = null

        private var tvTypeNumber: TextView =
            itemView.findViewById(R.id.tvTypeNumber)
        private var tvTypeDescription: TextView =
            itemView.findViewById(R.id.tvTypeDescription)

        init {
            itemView.setOnClickListener(this)
        }

        fun setData(plugPagType: PlugPagType, clickListener: ItemClickListener?) {
            this.plugPagType = plugPagType
            this.clickListener = clickListener

            this.tvTypeNumber.text = plugPagType.index.toString()
            this.tvTypeDescription.text = plugPagType.description
        }

        override fun onClick(view: View) {
            clickListener?.onItemClick(view, plugPagType)
        }
    }

    data class PlugPagType(val index: Int, val description: String, val type: Any) :
        Serializable {
        override fun toString() = "\"${index}, $description, ${type}\""
    }
}
