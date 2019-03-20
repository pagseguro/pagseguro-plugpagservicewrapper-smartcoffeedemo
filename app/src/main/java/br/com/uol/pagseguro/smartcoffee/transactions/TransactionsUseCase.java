package br.com.uol.pagseguro.smartcoffee.transactions;


import java.util.Random;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAbortResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import br.com.uol.pagseguro.smartcoffee.ActionResult;

public class TransactionsUseCase {

    public static final String USER_REFERENCE = "APPDEMO";
    private final PlugPag mPlugPag;

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
            mPlugPag.setPlugPagCustomPrinterLayout(getCustomPrinterDialog());
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.voidPayment(plugPagVoidData);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    private Observable<ActionResult> doPayment(final PlugPagPaymentData paymentData) {
        return Observable.create(emitter -> {
            mPlugPag.setPlugPagCustomPrinterLayout(getCustomPrinterDialog());
            ActionResult result = new ActionResult();
            setListener(emitter, result);
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

    private void setListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setEventListener(plugPagEventData -> {
            result.setMessage(plugPagEventData.getCustomMessage());
            emitter.onNext(result);
        });
    }

    private int getAmount() {
        return new Random().nextInt(10000) + 100;
    }

    private int getInstallments() {
        return new Random().nextInt(5) + 1;
    }

    public Observable<String> printStablishmentReceipt() {
        return Observable.create(emitter -> {

            PlugPagPrintResult result = mPlugPag.reprintStablishmentReceipt();

            if (result.getResult() != 0) {
                emitter.onError(new RuntimeException(result.getMessage()));
            }

            emitter.onComplete();
        });
    }

    public Observable<String> printCustomerReceipt() {
        return Observable.create(emitter -> {

            PlugPagPrintResult result = mPlugPag.reprintCustomerReceipt();

            if (result.getResult() != 0) {
                emitter.onError(new RuntimeException(result.getMessage()));
            }

            emitter.onComplete();
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
