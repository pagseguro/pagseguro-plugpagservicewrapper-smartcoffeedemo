package br.com.uol.pagseguro.smartcoffee.payments.installments;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

public interface SelectInstallmentContract extends MvpView {

    void showLoading(boolean show);

    void setUpAdapter(List<String> installments);

    void showError(String message);

    void showActivationDialog();

    void showAuthProgress(String message);
}
