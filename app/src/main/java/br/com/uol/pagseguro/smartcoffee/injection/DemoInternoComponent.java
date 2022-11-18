package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.payments.demoInterno.DemoInternoActivity;
import br.com.uol.pagseguro.smartcoffee.payments.demoInterno.DemoInternoPresenter;
import dagger.Component;

@Component(modules = {UseCaseModule.class, WrapperModule.class})
public interface DemoInternoComponent {

    void inject(DemoInternoActivity activity);

    DemoInternoPresenter presenter();
}
