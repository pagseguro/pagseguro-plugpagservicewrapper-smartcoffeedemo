package br.com.uol.pagseguro.smartcoffee.injection;

import dagger.Component;
import br.com.uol.pagseguro.smartcoffee.auth.AuthFragment;
import br.com.uol.pagseguro.smartcoffee.auth.AuthPresenter;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface AuthComponent extends MainComponent {

    AuthPresenter presenter();

    void inject(AuthFragment fragment);
}
