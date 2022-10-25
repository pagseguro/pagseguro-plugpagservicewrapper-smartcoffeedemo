package br.com.uol.pagseguro.smartcoffee.payments;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCardInfoResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagStyleData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.ActionResult;

import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.*;

import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class PaymentsUseCase {

    private final PlugPag mPlugPag;
    private PlugPagPaymentData mPlugPagPaymentData = null;

    public PaymentsUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }
    private static final String CUSTOM_PRINT_MESSAGE = "Teste: Imprimir via do cliente?";

    //Payment Methods

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

    public Observable<ActionResult> doQRCodePaymentInCashDebit(int value) {
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_DEBITO,
                value,
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    public Observable<ActionResult> doQRCodePaymentInCashCredit(int value) {
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_CREDITO,
                value,
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true,
                true,
                false
        ));
    }

    public Observable<ActionResult> doQRCodePaymentBuyerInstallments(int value, int installments) {
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_CREDITO,
                value,
                INSTALLMENT_TYPE_PARC_COMPRADOR,
                installments,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    public Observable<ActionResult> doQRCodePaymentSellerInstallments(int value, int installments) {
        return doPayment(new PlugPagPaymentData(
                TYPE_QRCODE_CREDITO,
                value,
                INSTALLMENT_TYPE_PARC_VENDEDOR,
                installments,
                USER_REFERENCE,
                true,
                false,
                false
        ));
    }

    // Payment and Refund Implementation

    private Observable<ActionResult> doPayment(final PlugPagPaymentData paymentData) {
        mPlugPagPaymentData = paymentData;
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setListener(emitter, result);
            setStyle();
            mPlugPag.setPlugPagCustomPrinterLayout(getCustomPrinterDialog());
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.doPayment(paymentData);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    public Observable<ActionResult> doRefund(ActionResult transaction) {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            setListener(emitter, actionResult);
            PlugPagTransactionResult result = mPlugPag.voidPayment(
                    new PlugPagVoidData(
                            transaction.getTransactionCode(),
                            transaction.getTransactionId(),
                            true
                    )
            );

            sendResponse(emitter, result, actionResult);
        });
    }

    private void sendResponse(
            ObservableEmitter<ActionResult> emitter,
            PlugPagTransactionResult plugPagTransactionResult,
            ActionResult result
    ) {
        if (plugPagTransactionResult.getResult() != 0) {
            emitter.onError(
                    new PlugPagException(
                            plugPagTransactionResult.getMessage(),
                            plugPagTransactionResult.getErrorCode()
                    )
            );
        } else {
            result.setTransactionCode(plugPagTransactionResult.getTransactionCode());
            result.setTransactionId(plugPagTransactionResult.getTransactionId());
            result.setTransactionResult(plugPagTransactionResult);
            emitter.onNext(result);
        }
        emitter.onComplete();
    }

    private void sendResponse(
            ObservableEmitter<ActionResult> emitter,
            PlugPagPrintResult printResult,
            ActionResult result
    ) {
        if (printResult.getResult() != 0) {
            result.setResult(printResult.getResult());
        }
        emitter.onComplete();
    }

    private void sendResponse(
            ObservableEmitter<ActionResult> emitter,
            PlugPagCardInfoResult cardResult,
            ActionResult result
    ) {
        if (cardResult.getResult() != null && !"0".equals(cardResult.getResult()) ||
                cardResult.getMessage() != null && !cardResult.getMessage().isEmpty()) {
            result.setMessage(cardResult.getMessage());
        } else {
            result.setMessage(
                    "Bin: " + cardResult.getCardHolder() + "\n" +
                            "Holder: " + cardResult.getHolder() + "\n" +
                            "CardHolder: " + cardResult.getCardHolder()
            );
        }

        emitter.onNext(result);
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

            PlugPagInitializationResult result =
                    mPlugPag.initializeAndActivatePinpad(
                            new PlugPagActivationData(activationCode)
                    );

            if (result.getResult() == PlugPag.RET_OK) {
                emitter.onNext(new ActionResult());
            } else {
                emitter.onError(new RuntimeException(result.getErrorMessage()));
            }

            emitter.onComplete();
        });
    }

    public Observable<ActionResult> getLastTransaction() {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();

            PlugPagTransactionResult result = mPlugPag.getLastApprovedTransaction();

            sendResponse(emitter, result, actionResult);
        });
    }

    public PlugPagPaymentData getEventPaymentData() {
        return mPlugPagPaymentData;
    }

    public Completable reboot() {
        return Completable.create(emitter -> {
            mPlugPag.reboot();
            emitter.onComplete();
        });
    }

    public Completable startOnboarding() {
        return Completable.create(emitter -> {
            mPlugPag.startOnboarding();
            emitter.onComplete();
        });
    }

    public Observable<ActionResult> getCardData() {
        return Observable.create(emitter -> {
            ActionResult action = new ActionResult();
            PlugPagCardInfoResult result = mPlugPag.getCardData();
            sendResponse(emitter, result, action);
        });
    }

    //Printer

    public Observable<ActionResult> printCustomerReceipt() {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            setPrintListener(emitter, actionResult);
            PlugPagPrintResult result = mPlugPag.reprintCustomerReceipt();
            sendResponse(emitter, result, actionResult);
        });
    }

    public Observable<ActionResult> printStablishmentReceipt() {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            setPrintListener(emitter, actionResult);
            PlugPagPrintResult result = mPlugPag.reprintStablishmentReceipt();
            sendResponse(emitter, result, actionResult);
        });
    }

    private void setPrintListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setPrinterListener(new PlugPagPrinterListener() {
            @Override
            public void onError(PlugPagPrintResult printResult) {
                result.setResult(printResult.getResult());
                result.setMessage(
                        String.format("Error %s %s", printResult.getErrorCode(), printResult.getMessage())
                );
                result.setErrorCode(printResult.getErrorCode());
                emitter.onNext(result);
            }

            @Override
            public void onSuccess(PlugPagPrintResult printResult) {
                result.setResult(printResult.getResult());
                result.setMessage(
                        String.format(
                                Locale.getDefault(), "Print OK: Steps [%d]", printResult.getSteps()
                        )
                );
                result.setErrorCode(printResult.getErrorCode());
                emitter.onNext(result);
            }
        });
    }

    //Custom Printer Layouts

    public PlugPagCustomPrinterLayout getCustomPrinterDialog() {
        PlugPagCustomPrinterLayout customDialog = new PlugPagCustomPrinterLayout();
        customDialog.setTitle(CUSTOM_PRINT_MESSAGE);
        customDialog.setMaxTimeShowPopup(60);
        return customDialog;
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

}
