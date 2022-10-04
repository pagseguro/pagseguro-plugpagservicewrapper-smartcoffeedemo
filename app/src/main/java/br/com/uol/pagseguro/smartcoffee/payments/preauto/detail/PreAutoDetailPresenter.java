package br.com.uol.pagseguro.smartcoffee.payments.preauto.detail;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoUseCase;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PreAutoDetailPresenter extends MvpNullObjectBasePresenter<PreAutoDetailContract> {

    private final PreAutoUseCase mUseCase;
    private Disposable mSubscribe;

    @Inject
    public PreAutoDetailPresenter(PreAutoUseCase useCase) {
        mUseCase = useCase;
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }

        super.detachView(retainInstance);
    }

    public void doPreAutoCancel(String transactionId, String transactionCode) {
        if (!Utils.isNotNullOrEmpty(transactionCode).equals(Utils.VALUE_NULL_OR_EMPTY) &&
                !Utils.isNotNullOrEmpty(transactionCode).equals(Utils.VALUE_NULL_OR_EMPTY)) {

            mSubscribe = mUseCase.doPreAutoCancel(transactionId, transactionCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    (ActionResult result) -> {
                        if (result.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD ||
                                result.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                            getView().showMessage(Utils.checkMessagePassword(result.getEventCode(), 0));
                        } else {
                            final PlugPagTransactionResult transactionResult = result.getTransactionResult();

                            if (transactionResult != null && transactionResult.getResult() == 0 && "0000".equals(transactionResult.getErrorCode())) {
                                getView().dismissDialog();
                                getView().showTransactionSuccess();
                                getView().closeActivity();
                            } else {
                                getView().showMessage(Utils.checkMessage(result.getMessage()));
                            }
                        }
                    },
                    error -> getView().showError(error.getMessage())
                );
        } else {
            getView().showError("TransactionId e transactionCode não pode ser vazio ou nulo");
        }
    }
}
