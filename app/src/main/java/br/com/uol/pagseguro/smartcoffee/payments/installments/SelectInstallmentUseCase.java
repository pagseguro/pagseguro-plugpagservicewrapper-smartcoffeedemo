package br.com.uol.pagseguro.smartcoffee.payments.installments;

import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_PARC_VENDEDOR;

import java.util.List;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import io.reactivex.Observable;

public class SelectInstallmentUseCase {

    private final PlugPag mPlugPag;

    public SelectInstallmentUseCase(PlugPag mPlugPag) {
        this.mPlugPag = mPlugPag;
    }

    public Observable<List<PlugPagInstallment>> calculateInstallments(
            Integer saleValue,
            Integer transactionType
    ) {
        return Observable.create(emitter -> {
            List<PlugPagInstallment> result;

            try {
                if (transactionType.equals(INSTALLMENT_TYPE_PARC_VENDEDOR)) {
                    result = calculateInstallmentsSeller(saleValue);
                } else {
                    result = calculateInstallmentsBuyer(saleValue);
                }
                emitter.onNext(result);
            } catch (PlugPagException exception) {
                emitter.onError(exception);
            }
            emitter.onComplete();
        });
    }

    public List<PlugPagInstallment> calculateInstallmentsSeller(Integer saleValue) {
        return mPlugPag.calculateInstallments(saleValue.toString(), PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR);
    }

    public List<PlugPagInstallment> calculateInstallmentsBuyer(Integer saleValue) {
        return mPlugPag.calculateInstallments(saleValue.toString(), PlugPag.INSTALLMENT_TYPE_PARC_COMPRADOR);
    }

}
