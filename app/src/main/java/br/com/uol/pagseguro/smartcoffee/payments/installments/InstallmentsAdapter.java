package br.com.uol.pagseguro.smartcoffee.payments.installments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;

public class InstallmentsAdapter extends RecyclerView.Adapter<InstallmentsAdapter.ViewHolder> {

    private final List<String> installments;
    private final OnItemClickListener listener;

    public InstallmentsAdapter(List<String> installments, OnItemClickListener listener) {
        this.installments = installments;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_installment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(installments.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return installments.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String installNumber);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemInstallment;

        public ViewHolder(View itemView) {
            super(itemView);
            itemInstallment = itemView.findViewById(R.id.tvInstallment);
        }

        public void bind(String installment, final int item, final OnItemClickListener listener) {
            if (installment != null) {
                final Integer installNumber = item + 1;
                itemInstallment.setText(installNumber + " x " + Utils.getFormattedValue(Double.parseDouble(installment)));
                itemView.setOnClickListener(v -> listener.onItemClick(installNumber.toString()));
            }

        }

    }
}
