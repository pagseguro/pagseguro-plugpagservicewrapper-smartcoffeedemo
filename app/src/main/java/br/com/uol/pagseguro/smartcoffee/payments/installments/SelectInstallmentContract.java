package br.com.uol.pagseguro.smartcoffee.payments.installments;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment;

public interface SelectInstallmentContract extends MvpView {

    void showLoading(boolean show);

    void setUpAdapter(List<PlugPagInstallment> installments);

    void showMessage(String message);
}
