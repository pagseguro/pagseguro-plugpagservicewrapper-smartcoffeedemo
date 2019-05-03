package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.demo.DemoInternoActivity;
import br.com.uol.pagseguro.smartcoffee.demo.DemoInternoPresenter;
import dagger.Component;

@Component(modules = {UseCaseModule.class, WrapperModule.class})
public interface DemoInternoComponent {

    void inject(DemoInternoActivity activity);

    DemoInternoPresenter presenter();
}
