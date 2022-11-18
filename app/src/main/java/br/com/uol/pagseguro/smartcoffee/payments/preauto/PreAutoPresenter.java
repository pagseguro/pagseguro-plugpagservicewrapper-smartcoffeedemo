package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoQueryData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PreAutoPresenter extends MvpNullObjectBasePresenter<PreAutoContract> {

    private static final int RET_OK = 0;
    private static final String SUCCESS_CODE = "0000";

    private final PreAutoUseCase mUseCase;

    private Disposable mSubscribe;

    @Inject
    public PreAutoPresenter(PreAutoUseCase useCase) {
        mUseCase = useCase;
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
        super.detachView(retainInstance);
    }

    public void abortTransaction() {
        mSubscribe = mUseCase.abort()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void doPreAutoCreation(int value, int installmentType, int installments) {
        doAction(mUseCase.doPreAutoCreate(value, installmentType, installments, null, null, null), value);
    }

    public void doPreAutoCreation(int value, int installmentType, int installments,
                                  String securityCode, String expirationDate, String pan) {
        doAction(mUseCase.doPreAutoCreate(value, installmentType, installments, securityCode, expirationDate, pan), value);
    }

    public void doPreAutoEffectuate(
            int value,
            String transactionId,
            String transactionCode
    ) {
        mSubscribe = mUseCase.doPreAutoEffectuate(value, transactionId, transactionCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .doOnComplete(() -> getView().showTransactionDialog("Pre autorização foi efetivada com sucesso"))
                .subscribe(
                        (ActionResult result) -> {
                            if (result.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD ||
                                    result.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                                getView().showTransactionDialog(Utils.checkMessagePassword(result.getEventCode(), value));
                            } else {
                                getView().showTransactionDialog(result.getMessage());
                            }
                        },
                        throwable -> getView().showTransactionDialog(throwable.getMessage())
                );
    }

    public void getPreAutoDataEffectivate(DismissListenerEffectivate dismissListener,
                                          @Nullable PlugPagPreAutoQueryData preAutoQueryData) {
        mSubscribe = mUseCase.getPreAutoData(preAutoQueryData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(
                        (ActionResult result) -> {
                            if (result.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD ||
                                    result.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                                getView().showTransactionDialog(
                                        Utils.checkMessagePassword(result.getEventCode(), 0)
                                );
                            } else {
                                final PlugPagTransactionResult transactionResult = result.getTransactionResult();

                                if (transactionResult != null &&
                                        transactionResult.getResult() != null &&
                                        transactionResult.getResult() == RET_OK) {
                                    if (transactionResult.getAmount() != null &&
                                            !transactionResult.getAmount().isEmpty()) {
                                        getView().dismissDialog();
                                        getView().showDialogValuePreAuto(
                                                dismissListener,
                                                transactionResult
                                        );
                                    }
                                } else {
                                    getView().showTransactionDialog(result.getMessage());
                                }
                            }
                        },
                        throwable -> getView().showTransactionDialog(throwable.getMessage())
                );
    }

    public void getPreAutoData(Boolean isPreAutoCancel, @Nullable PlugPagPreAutoQueryData preAutoQueryData) {
        mSubscribe = mUseCase.getPreAutoData(preAutoQueryData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(
                        (ActionResult result) -> {
                            if (result.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD ||
                                    result.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                                getView().showTransactionDialog(Utils.checkMessagePassword(result.getEventCode(), 0));
                            } else {
                                final PlugPagTransactionResult transactionResult = result.getTransactionResult();

                                if (transactionResult != null &&
                                        transactionResult.getResult() == 0 &&
                                        SUCCESS_CODE.equals(transactionResult.getErrorCode())) {
                                    getView().dismissDialog();
                                    getView().showPreAutoDetail(result.getTransactionResult(), isPreAutoCancel);
                                } else {
                                    getView().showTransactionDialog(Utils.checkMessage(result.getMessage()));
                                }

                            }
                        },
                        error -> getView().showTransactionDialog(error.getMessage())
                );
    }

    private void doAction(Observable<ActionResult> action, int value) {
        mSubscribe = action
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showTransactionDialog("CARREGANDO"))
                .doOnComplete(() -> getView().showTransactionSuccess())
                .subscribe(
                        (ActionResult result) -> {
                            if (result.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD ||
                                    result.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                                getView().showTransactionDialog(Utils.checkMessagePassword(result.getEventCode(), value));
                            } else {
                                getView().showTransactionDialog(result.getMessage());
                            }
                        },
                        throwable -> getView().showTransactionDialog(throwable.getMessage())
                );
    }

}
