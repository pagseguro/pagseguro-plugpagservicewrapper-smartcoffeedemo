package uol.pagseguro.com.br.smartcoffee.transactions;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TransactionsPresenter extends MvpNullObjectBasePresenter<TransactionsContract> {

    private TransactionsUseCase mUseCase;
    private Disposable mSubscribe;
    private Boolean hasAborted = false;

    @Inject
    public TransactionsPresenter(TransactionsUseCase useCase) {
        mUseCase = useCase;
    }

    public void creditPayment() {
        doPayment(mUseCase.doCreditPayment());
    }

    public void doCreditPaymentWithSellerInstallments() {
        doPayment(mUseCase.doCreditPaymentWithSellerInstallments());
    }

    public void doCreditPaymentWithBuyerInstallments() {
        doPayment(mUseCase.doCreditPaymentWithBuyerInstallments());
    }

    public void doDebitPayment() {
        doPayment(mUseCase.doDebitPayment());
    }


    public void doVoucherPayment() {
        doPayment(mUseCase.doVoucherPayment());
    }

    public void doRefundPayment() {
        doPayment(mUseCase.doRefundPayment());
    }

    private void doPayment(Observable<String> observable) {
        mSubscribe = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> getView().showPaymentSuccess())
                .subscribe(message -> getView().showMessage(message),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void abort() {
        mSubscribe = mUseCase.abort()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> hasAborted = true)
                .subscribe(o -> getView().showAbortedSuccessfully(),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void abortTransaction() {
        if (hasAborted) {
            hasAborted = false;
            return;
        }

        abort();
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
        super.detachView(retainInstance);
    }

}
