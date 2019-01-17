package uol.pagseguro.com.br.smartcoffee.transactions;

import com.hannesdorfmann.mosby.mvp.MvpView;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public interface TransactionsContract extends MvpView {

    void showTransactionSuccess();

    void showError(String message);

    void showMessage(String message);

    void showLoading(boolean show);

    void writeToFile(String transactionCode, String transactionId);

    void showAbortedSuccessfully();
}
