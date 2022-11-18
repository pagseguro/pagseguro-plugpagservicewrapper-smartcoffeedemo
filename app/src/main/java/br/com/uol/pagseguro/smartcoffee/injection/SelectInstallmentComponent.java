package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.payments.installments.SelectInstallmentActivity;
import br.com.uol.pagseguro.smartcoffee.payments.installments.SelectInstallmentPresenter;
import dagger.Component;

@Component(modules = {UseCaseModule.class, WrapperModule.class})
public interface SelectInstallmentComponent {

    void inject(SelectInstallmentActivity activity);

    SelectInstallmentPresenter presenter();
}
