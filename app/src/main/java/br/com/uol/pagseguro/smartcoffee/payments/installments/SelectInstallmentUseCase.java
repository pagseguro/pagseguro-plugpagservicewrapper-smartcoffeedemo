package br.com.uol.pagseguro.smartcoffee.payments.installments;

import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_PARC_VENDEDOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.Observable;

public class SelectInstallmentUseCase {

    private final PlugPag mPlugPag;
    private static final int INSTALLMENT_MAX_LENGHT = 17;
    private static final int MIN_VALUE_INSTALLMENT = 500;

    public SelectInstallmentUseCase(PlugPag mPlugPag) {
        this.mPlugPag = mPlugPag;
    }

    public Observable<Boolean> isAuthenticated() {
        return Observable.create(emitter -> {
            emitter.onNext(mPlugPag.isAuthenticated());
            emitter.onComplete();
        });
    }

    public Observable<List<String>> calculateInstallments(Integer saleValue,
                                                          Integer transactionType,
                                                          Boolean isPreAutoKeyed) {
        return Observable.create(emitter -> {
            List<String> result;

            if (transactionType.equals(INSTALLMENT_TYPE_PARC_VENDEDOR)) {
                result = calculateInstallmentsSeller(saleValue, isPreAutoKeyed);
            } else {
                result = calculateInstallmentsBuyer(saleValue);
            }

            if (result.size() > 0) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException());
            }
            emitter.onComplete();
        });
    }

    public List<String> calculateInstallmentsSeller(int saleValue, boolean isPreAutoKeyed) {
        List<String> installments = new ArrayList<>();
        for (int i = 0; i <= INSTALLMENT_MAX_LENGHT; i++) {
            int installmentValue = (saleValue / (i + 1));
            if (isPreAutoKeyed) {
                if (installmentValue >= MIN_VALUE_INSTALLMENT) {
                    installments.add(String.valueOf(installmentValue));
                } else {
                    break;
                }
            } else {
                installments.add(String.valueOf(installmentValue));
            }
        }
        return installments;
    }

    public List<String> calculateInstallmentsBuyer(Integer saleValue) {
        return Arrays.asList(mPlugPag.calculateInstallments(saleValue.toString()));
    }

    public Observable<ActionResult> initializeAndActivatePinpad(String activationCode) {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            mPlugPag.setEventListener(plugPagEventData -> {
                actionResult.setEventCode(plugPagEventData.getEventCode());
                actionResult.setMessage(plugPagEventData.getCustomMessage());
                emitter.onNext(actionResult);
            });

            PlugPagInitializationResult result = mPlugPag.initializeAndActivatePinpad(
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
}
