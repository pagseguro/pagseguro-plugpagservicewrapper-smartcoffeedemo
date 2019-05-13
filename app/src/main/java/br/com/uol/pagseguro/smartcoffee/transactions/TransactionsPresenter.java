package br.com.uol.pagseguro.smartcoffee.transactions;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TransactionsPresenter extends MvpNullObjectBasePresenter<TransactionsContract> {

    private TransactionsUseCase mUseCase;
    private Disposable mSubscribe;
    private Boolean hasAborted = false;
    private int countPassword = 0;

    @Inject
    public TransactionsPresenter(TransactionsUseCase useCase) {
        mUseCase = useCase;
    }

    public void creditPayment() {
        doAction(mUseCase.doCreditPayment());
    }

    public void doCreditPaymentWithSellerInstallments() {
        doAction(mUseCase.doCreditPaymentWithSellerInstallments());
    }

    public void doCreditPaymentWithBuyerInstallments() {
        doAction(mUseCase.doCreditPaymentWithBuyerInstallments());
    }

    public void doDebitPayment() {
        doAction(mUseCase.doDebitPayment());
    }

    public void doVoucherPayment() {
        doAction(mUseCase.doVoucherPayment());
    }

    public void doRefundPayment(ActionResult actionResult) {
        doAction(mUseCase.doRefundPayment(actionResult));
    }

    private void doAction(Observable<ActionResult> action) {
        mSubscribe = action.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> getView().showTransactionSuccess())
                .subscribe((ActionResult result) -> {
                            writeToFile(result);

                            if (result.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD ||
                                    result.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
                                getView().showMessage(checkMessagePassword(result.getEventCode()));
                            } else if (result.getErrorCode() != null) {
                                getView().showPrintError(result.getMessage());
                            } else {
                                getView().showMessage(result.getMessage());
                            }

                        },
                        throwable -> {
                            hasAborted = true;
                            getView().showError(throwable.getMessage());
                        });
    }

    private String checkMessagePassword(int eventCode) {
        StringBuilder strPassword = new StringBuilder();

        int value = mUseCase.getEventPaymentData() != null ? mUseCase.getEventPaymentData().getAmount() : 0;

        if (eventCode == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
            countPassword++;
        }

        if (eventCode == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
            countPassword = 0;
        }

        for (int count = countPassword; count > 0; count--) {
            strPassword.append("*");
        }

        return String.format("VALOR: %.2f\nSENHA: %s", (value / 100.0), strPassword.toString());
    }

    private void writeToFile(ActionResult result) {
        if (result.getTransactionCode() != null && result.getTransactionId() != null) {
            getView().writeToFile(result.getTransactionCode(), result.getTransactionId());
        }
    }


    public void abortTransaction() {
        if (hasAborted) {
            hasAborted = false;
            return;
        }

        mSubscribe = mUseCase.abort()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> hasAborted = true)
                .doOnDispose(() -> hasAborted = true)
                .subscribe(o -> getView().showAbortedSuccessfully(),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
        super.detachView(retainInstance);
    }

    public void printStablishmentReceipt() {
        mSubscribe = mUseCase.printStablishmentReceipt()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> getView().showLoading(false))
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .subscribe(message -> getView().showTransactionSuccess(message.getMessage()),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void printCustomerReceipt() {
        mSubscribe = mUseCase.printCustomerReceipt()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> getView().showLoading(false))
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doOnError(throwable -> getView().showError(throwable.getMessage()))
                .subscribe(message -> getView().showTransactionSuccess(message.getMessage()),
                        throwable -> getView().showError(throwable.getMessage()));
    }


    public void getLastTransaction() {
        mSubscribe = mUseCase.getLastTransaction()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(actionResult -> getView().showLastTransaction(actionResult.getTransactionCode()),
                        throwable -> getView().showError(throwable.getMessage()));
    }
}
