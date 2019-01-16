package uol.pagseguro.com.br.smartcoffee.transactions;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import uol.pagseguro.com.br.smartcoffee.ActionResult;

public class TransactionsPresenter extends MvpNullObjectBasePresenter<TransactionsContract> {

    private TransactionsUseCase mUseCase;
    private Disposable mSubscribe;

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
                            getView().showMessage(result.getMessage());
                        },
                        throwable -> getView().showError(throwable.getMessage()));
    }

    private void writeToFile(ActionResult result) {
        if (result.getTransactionCode() != null && result.getTransactionId() != null) {
            getView().writeToFile(result.getTransactionCode(), result.getTransactionId());
        }
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
        super.detachView(retainInstance);
    }

    public void printReceipt() {
        mSubscribe = mUseCase.printCostumerReceipt()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> getView().showLoading(false))
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .subscribe(message -> getView().showMessage(message), throwable -> getView().showError(throwable.getMessage()));
    }
}
