package br.com.uol.pagseguro.smartcoffee.payments.credit;

import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_TYPE_A_VISTA;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_TYPE_PARC_COMPRADOR;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_TYPE_PARC_VENDEDOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CONTENT_TEXT_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CONTENT_TEXT_VALUE_1_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CONTENT_TEXT_VALUE_2_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.HEAD_BACKGROUND_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.HEAD_TEXT_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.LINE_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.NEGATIVE_BUTTON_BACKGROUND;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.NEGATIVE_BUTTON_TEXT_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.POSITIVE_BUTTON_BACKGROUND;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.POSITIVE_BUTTON_TEXT_COLOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.TYPE_CREDITO;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagStyleData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class CreditPaymentUseCase {

    public static final String USER_REFERENCE = null;
    private final PlugPag mPlugPag;

    public CreditPaymentUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<ActionResult> doCreditPayment(int value, boolean isCarne) {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                value,
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true,
                true,
                isCarne
        ));
    }

    public Observable<ActionResult> doCreditPaymentBuyerInstallments(int value, int installments) {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                value,
                INSTALLMENT_TYPE_PARC_COMPRADOR,
                installments,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    public Observable<ActionResult> doCreditPaymentSellerInstallments(int value, int installments) {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                value,
                INSTALLMENT_TYPE_PARC_VENDEDOR,
                installments,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    private Observable<ActionResult> doPayment(final PlugPagPaymentData paymentData) {
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setStyle();
            setListener(emitter, result);
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.doPayment(paymentData);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    private void setStyle() {
        mPlugPag.setStyleData(
                new PlugPagStyleData(
                        HEAD_TEXT_COLOR,
                        HEAD_BACKGROUND_COLOR,
                        CONTENT_TEXT_COLOR,
                        CONTENT_TEXT_VALUE_1_COLOR,
                        CONTENT_TEXT_VALUE_2_COLOR,
                        POSITIVE_BUTTON_TEXT_COLOR,
                        POSITIVE_BUTTON_BACKGROUND,
                        NEGATIVE_BUTTON_TEXT_COLOR,
                        NEGATIVE_BUTTON_BACKGROUND,
                        LINE_COLOR
                )
        );
    }

    private void setListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setEventListener(plugPagEventData -> {
            result.setEventCode(plugPagEventData.getEventCode());
            result.setMessage(plugPagEventData.getCustomMessage());
            emitter.onNext(result);
        });
    }

    private void sendResponse(ObservableEmitter<ActionResult> emitter, PlugPagTransactionResult plugPagTransactionResult,
                              ActionResult result) {
        if (plugPagTransactionResult.getResult() != 0) {
            emitter.onError(new PlugPagException(plugPagTransactionResult.getMessage(), plugPagTransactionResult.getErrorCode()));
        } else {
            result.setTransactionCode(plugPagTransactionResult.getTransactionCode());
            result.setTransactionId(plugPagTransactionResult.getTransactionId());
            result.setTransactionResult(plugPagTransactionResult);
            emitter.onNext(result);
        }
        emitter.onComplete();
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

    public Completable abort() {
        return Completable.create(emitter -> mPlugPag.abort());
    }
}
