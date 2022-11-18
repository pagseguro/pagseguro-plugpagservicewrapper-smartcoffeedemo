package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.payments.credit.CreditPaymentActivity;
import br.com.uol.pagseguro.smartcoffee.payments.credit.CreditPaymentPresenter;
import dagger.Component;

@Component(
    modules = {
        UseCaseModule.class,
        WrapperModule.class
    }
)
public interface CreditComponent {
    void inject(CreditPaymentActivity activity);
    CreditPaymentPresenter presenter();
}
