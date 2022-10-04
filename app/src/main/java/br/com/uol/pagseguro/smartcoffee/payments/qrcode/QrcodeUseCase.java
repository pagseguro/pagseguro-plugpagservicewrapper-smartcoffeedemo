package br.com.uol.pagseguro.smartcoffee.payments.qrcode;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagStyleData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.*;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class QrcodeUseCase {

    public static final String USER_REFERENCE = null;
    private final PlugPag mPlugPag;

    public QrcodeUseCase(PlugPag plugPag){
        mPlugPag = plugPag;
    }

    public Observable<ActionResult> doQRCodePaymentInCashDebit(int value){
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_DEBITO,
                value,
                InstallmentConstants.INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    public Observable<ActionResult> doQRCodePaymentInCashCredit(int value){
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_CREDITO,
                value,
                InstallmentConstants.INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true,
                true,
                false
        ));
    }

    public Observable<ActionResult> doQRCodePaymentBuyerInstallments(int value, int installments){
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_CREDITO,
                value,
                InstallmentConstants.INSTALLMENT_TYPE_PARC_COMPRADOR,
                installments,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    public Observable<ActionResult> doQRCodePaymentSellerInstallments(int value,int installments){
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_CREDITO,
                value,
                InstallmentConstants.INSTALLMENT_TYPE_PARC_VENDEDOR,
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
