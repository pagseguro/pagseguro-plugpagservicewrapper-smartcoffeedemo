package br.com.uol.pagseguro.smartcoffee.demo;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagStyleData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.*;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.*;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class DemoInternoUseCase {

    private final PlugPag mPlugPag;

    public DemoInternoUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<ActionResult> doCreditPayment(int value) {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                value,
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true
        ));
    }

    public Observable<ActionResult> doDebitPayment(int value, boolean isCarne) {
        return doPayment(new PlugPagPaymentData(
                TYPE_DEBITO,
                value,
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true,
                false,
                isCarne));
    }

    public Observable<ActionResult> doVoucherPayment(int value) {
        return doPayment(new PlugPagPaymentData(
                TYPE_VOUCHER,
                value,
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true));
    }

    public Observable<ActionResult> doPixPayment(int value) {
        return doPayment(new PlugPagPaymentData(
                TYPE_PIX,
                value,
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    private Observable<ActionResult> doPayment(final PlugPagPaymentData paymentData) {
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setListener(emitter, result);
            mPlugPag.setStyleData(
                    new PlugPagStyleData(
                            (int) 0xffffffff,
                            (int) 0xff1ec390,
                            (int) 0xff202020,
                            (int) 0xff002000,
                            (int) 0xfff00000,
                            (int) 0xffffffff,
                            (int) 0xff00ca74,
                            (int) 0xff888888,
                            (int) 0x00ffffff,
                            (int) 0xff000000
                    )
            );
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.doPayment(paymentData);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    private void sendResponse(ObservableEmitter<ActionResult> emitter, PlugPagTransactionResult plugPagTransactionResult,
                              ActionResult result) {
        if (
                plugPagTransactionResult.getResult() != 0 ||
                plugPagTransactionResult.getTransactionCode() == null ||
                plugPagTransactionResult.getTransactionCode().length() == 0
        ) {
            emitter.onError(new PlugPagException(plugPagTransactionResult.getMessage(), plugPagTransactionResult.getErrorCode()));
        } else {
            result.setTransactionCode(plugPagTransactionResult.getTransactionCode());
            result.setTransactionId(plugPagTransactionResult.getTransactionId());
            result.setTransactionResult(plugPagTransactionResult);
            emitter.onNext(result);
        }
        emitter.onComplete();
    }

    private void setListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setEventListener(plugPagEventData -> {
            result.setEventCode(plugPagEventData.getEventCode());
            result.setMessage(plugPagEventData.getCustomMessage());
            emitter.onNext(result);
        });
    }

    public Completable abort() {
        return Completable.create(emitter -> mPlugPag.abort());
    }

    public Observable<Boolean> isAuthenticated() {
        return Observable.create(emitter -> {
            emitter.onNext(mPlugPag.isAuthenticated());
            emitter.onComplete();
        });
    }

    public Observable<ActionResult> initializeAndActivatePinpad(String activationCode) {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            mPlugPag.setEventListener(plugPagEventData -> {
                actionResult.setEventCode(plugPagEventData.getEventCode());
                actionResult.setMessage(plugPagEventData.getCustomMessage());
                emitter.onNext(actionResult);
            });

            PlugPagInitializationResult result = mPlugPag.initializeAndActivatePinpad(new PlugPagActivationData(activationCode));
            if (result.getResult() == PlugPag.RET_OK) {
                emitter.onNext(new ActionResult());
            } else {
                emitter.onError(new RuntimeException(result.getErrorMessage()));
            }
            emitter.onComplete();
        });
    }

    public Observable<ActionResult> doRefund(ActionResult transaction) {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            setListener(emitter, actionResult);
            PlugPagTransactionResult result = mPlugPag.voidPayment(new PlugPagVoidData(transaction.getTransactionCode(),
                    transaction.getTransactionId(),
                    true));

            sendResponse(emitter, result, actionResult);
        });
    }

    public Observable<ActionResult> getLastTransaction() {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();

            PlugPagTransactionResult result = mPlugPag.getLastApprovedTransaction();

            sendResponse(emitter, result, actionResult);
        });
    }
}
