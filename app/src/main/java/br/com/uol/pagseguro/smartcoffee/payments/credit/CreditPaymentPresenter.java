package br.com.uol.pagseguro.smartcoffee.payments.credit;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.payments.PaymentsUseCase;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CreditPaymentPresenter extends MvpNullObjectBasePresenter<CreditPaymentContract> {

    private PaymentsUseCase mUseCase;
    private Disposable mSubscribe;
    private int countPassword = 0;
    private static final String ASTERISK = "*";

    @Inject
    public CreditPaymentPresenter(PaymentsUseCase useCase) {
        this.mUseCase = useCase;
    }

    public void creditPaymentInCash(int value) {
        doAction(mUseCase.doCreditPayment(value, false), value);
    }

    public void creditPaymentBuyerInstallments(int value, int installments) {
        doAction(mUseCase.doCreditPaymentBuyerInstallments(value, installments), value);
    }

    public void creditPaymentSellerInstallments(int value, int installments) {
        doAction(mUseCase.doCreditPaymentSellerInstallments(value, installments), value);
    }

    private void doAction(Observable<ActionResult> action, int value) {
        mSubscribe = action
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> getView().showTransactionSuccess())
                .doOnDispose(() -> getView().disposeDialog())
                .subscribe((ActionResult result) -> {
                            writeToFile(result);
                            if (result.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD ||
                                    result.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                                getView().showTransactionDialog(checkMessagePassword(result.getEventCode(), value));
                            } else {
                                getView().showTransactionDialog(checkMessage(result.getMessage()));
                            }
                        },
                        throwable -> {
                            getView().showTransactionDialog(throwable.getMessage());
                            getView().disposeDialog();
                        }
                );
    }

    private void writeToFile(ActionResult result) {
        if (result.getTransactionCode() != null && result.getTransactionId() != null) {
            getView().writeToFile(result.getTransactionCode(), result.getTransactionId());
        }
    }

    private String checkMessagePassword(int eventCode, int value) {
        StringBuilder strPassword = new StringBuilder();

        if (eventCode == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
            countPassword++;
        }
        if (eventCode == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
            countPassword = 0;
        }

        for (int count = countPassword; count > 0; count--) {
            strPassword.append(ASTERISK);
        }

        return String.format("VALOR: %.2f\nSENHA: %s", (value / 100.0), strPassword);
    }

    private String checkMessage(String message) {
        if (message != null && message.contains(String.valueOf(R.string.text_senha)) &&
                !message.contains(String.valueOf(R.string.text_incorreta))) {
            String[] strings = message.split(String.valueOf(R.string.text_senha));
            return strings[0].trim();
        }
        return message;
    }

    public void abortTransaction() {
        mSubscribe = mUseCase.abort()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
