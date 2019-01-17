package uol.pagseguro.com.br.smartcoffee.transactions;


import org.jetbrains.annotations.NotNull;

import java.util.Random;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAbortResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;

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

    public Observable<String> doCreditPayment() {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                getAmount(),
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true
        ));
    }

    public Observable<String> doCreditPaymentWithSellerInstallments() {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                getAmount(),
                INSTALLMENT_TYPE_PARC_VENDEDOR,
                getInstallments(),
                USER_REFERENCE,
                true));
    }

    public Observable<String> doCreditPaymentWithBuyerInstallments() {
        return doPayment(new PlugPagPaymentData(
                TYPE_CREDITO,
                getAmount(),
                INSTALLMENT_TYPE_PARC_COMPRADOR,
                getInstallments(),
                USER_REFERENCE,
                true));
    }


    public Observable<String> doDebitPayment() {
        return doPayment(new PlugPagPaymentData(
                TYPE_DEBITO,
                getAmount(),
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true));
    }

    public Observable<String> doVoucherPayment() {
        return doPayment(new PlugPagPaymentData(
                TYPE_VOUCHER,
                getAmount(),
                INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true));
    }

    public Observable<String> doRefundPayment() {
        return doPayment(new PlugPagPaymentData(
                PlugPag.TYPE_CREDITO,
                getAmount(),
                PlugPag.INSTALLMENT_TYPE_A_VISTA,
                1,
                USER_REFERENCE,
                true));
    }

    private Observable<String> doPayment(final PlugPagPaymentData paymentData) {
        return Observable.create(emitter -> {

            mPlugPag.setEventListener(plugPagEventData -> emitter.onNext(plugPagEventData.getCustomMessage()));

            PlugPagTransactionResult result = mPlugPag.doPayment(paymentData);

            if (result.getResult() != 0) {
                emitter.onError(new RuntimeException(result.getMessage()));
            }

            emitter.onComplete();
        });
    }

    private int getAmount() {
        return new Random().nextInt(10000) + 100;
    }

    private int getInstallments() {
        return new Random().nextInt(5) + 1;
    }

    public Observable<Object> abort() {
        return Observable.create(emitter -> {

            PlugPagAbortResult result = mPlugPag.abort();

            if (result.getResult() == 0) {
                emitter.onNext(new Object());
            } else {
                emitter.onError(new Exception("Erro ao abortar"));
            }

            emitter.onComplete();
        });
    }
}
