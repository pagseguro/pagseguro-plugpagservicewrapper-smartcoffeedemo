package uol.pagseguro.com.br.smartcoffee.transactions;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface TransactionsContract extends MvpView {

    void showPaymentSuccess();

    void showError(String message);

    void showMessage(String message);

    void setCancelableDialog();
}
