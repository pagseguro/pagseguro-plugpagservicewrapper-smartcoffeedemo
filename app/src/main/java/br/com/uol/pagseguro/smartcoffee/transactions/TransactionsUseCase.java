package br.com.uol.pagseguro.smartcoffee.transactions;


import java.util.Random;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAbortResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class TransactionsUseCase {

    public static final String USER_REFERENCE = "APPDEMO";
    private final PlugPag mPlugPag;
    private PlugPagPaymentData mPlugPagPaymentData = null;
    private final int TYPE_CREDITO = 1;
    private final int TYPE_DEBITO = 2;
    private final int TYPE_VOUCHER = 3;

    private final int INSTALLMENT_TYPE_A_VISTA = 1;
    private final int INSTALLMENT_TYPE_PARC_VENDEDOR = 2;
    private final int INSTALLMENT_TYPE_PARC_COMPRADOR = 3;

    public TransactionsUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<ActionResult> doCreditPayment() {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                getAmount(),
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true
        ));
    }

    public Observable<ActionResult> doCreditPaymentWithSellerInstallments() {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                getAmount(),
                INSTALLMENT_TYPE_PARC_VENDEDOR,
                getInstallments(),
                USER_REFERENCE,
                true));
    }

    public Observable<ActionResult> doCreditPaymentWithBuyerInstallments() {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                getAmount(),
                INSTALLMENT_TYPE_PARC_COMPRADOR,
                getInstallments(),
                USER_REFERENCE,
                true));
    }


    public Observable<ActionResult> doDebitPayment() {
        return doPayment(new PlugPagPaymentData(
                TYPE_DEBITO,
                getAmount(),
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true));
    }

    public Observable<ActionResult> doVoucherPayment() {
        return doPayment(new PlugPagPaymentData(
                TYPE_VOUCHER,
                getAmount(),
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true));
    }

    public Observable<ActionResult> doRefundPayment(ActionResult actionResult) {
        if (actionResult.getTransactionCode() == null) {
            return Observable.error(new Exception("Nenhuma transação encontrada"));
        }
        return doRefund(new PlugPagVoidData(actionResult.getTransactionCode(), actionResult.getTransactionId(), true));
    }

    private Observable<ActionResult> doRefund(final PlugPagVoidData plugPagVoidData) {
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setListener(emitter, result);
            setPrintListener(emitter, result);
            mPlugPag.setPlugPagCustomPrinterLayout(getCustomPrinterDialog());
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.voidPayment(plugPagVoidData);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    private Observable<ActionResult> doPayment(final PlugPagPaymentData paymentData) {
        mPlugPagPaymentData = paymentData;
        return Observable.create(emitter -> {
            mPlugPag.setPlugPagCustomPrinterLayout(getCustomPrinterDialog());
            ActionResult result = new ActionResult();
            setListener(emitter, result);
            setPrintListener(emitter, result);
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.doPayment(paymentData);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    private void sendResponse(ObservableEmitter<ActionResult> emitter, PlugPagTransactionResult plugPagTransactionResult,
                              ActionResult result) {
        if (plugPagTransactionResult.getResult() != 0) {
            emitter.onError(new RuntimeException(plugPagTransactionResult.getMessage()));
        } else {
            result.setTransactionCode(plugPagTransactionResult.getTransactionCode());
            result.setTransactionId(plugPagTransactionResult.getTransactionId());
            emitter.onNext(result);
        }
        emitter.onComplete();
    }

    private void sendResponse(ObservableEmitter<ActionResult> emitter, PlugPagPrintResult printResult,
                              ActionResult result) {

        if (printResult.getResult() != 0)
            result.setResult(printResult.getResult());

        emitter.onComplete();
    }

    private void setListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setEventListener(plugPagEventData -> {
            result.setEventCode(plugPagEventData.getEventCode());
            result.setMessage(plugPagEventData.getCustomMessage());
            emitter.onNext(result);
        });
    }

    private void setPrintListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setPrinterListener(printResult -> {
            result.setMessage(printResult.getMessage());
            result.setErrorCode(printResult.getErrorCode());
            result.setResult(printResult.getResult());
            emitter.onNext(result);
        });
    }

    public PlugPagPaymentData getEventPaymentData(){
        return mPlugPagPaymentData;
    }

    private int getAmount() {
        return new Random().nextInt(10000) + 100;
    }

    private int getInstallments() {
        return new Random().nextInt(5) + 1;
    }

    public Observable<ActionResult> printStablishmentReceipt() {
        return Observable.create(emitter -> {

            ActionResult actionResult = new ActionResult();
            setPrintListener(emitter, actionResult);
            PlugPagPrintResult result = mPlugPag.reprintStablishmentReceipt();
            sendResponse(emitter, result, actionResult);
        });
    }

    public Observable<ActionResult> printCustomerReceipt() {
        return Observable.create(emitter -> {

            ActionResult actionResult = new ActionResult();
            setPrintListener(emitter, actionResult);
            PlugPagPrintResult result = mPlugPag.reprintCustomerReceipt();
            sendResponse(emitter, result, actionResult);
        });
    }

    public Observable<Object> abort() {
        return Observable.create(emitter -> {
            mPlugPag.abort();
            PlugPagAbortResult result = mPlugPag.abort();

            if (result.getResult() == 0) {
                emitter.onNext(new Object());
            } else {
                emitter.onError(new Exception("Erro ao abortar"));
            }

            emitter.onComplete();
        });
    }

    public PlugPagCustomPrinterLayout getCustomPrinterDialog() {
        PlugPagCustomPrinterLayout customDialog = new PlugPagCustomPrinterLayout();
        customDialog.setTitle("Teste: Imprimir via do client?");
        customDialog.setButtonBackgroundColor("#00ff33");
        customDialog.setConfirmText("Yes");
        customDialog.setCancelText("No");
        return customDialog;
    }
}
