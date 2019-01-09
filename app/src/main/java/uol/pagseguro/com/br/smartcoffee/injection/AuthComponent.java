package uol.pagseguro.com.br.smartcoffee.injection;

import dagger.Component;
import uol.pagseguro.com.br.smartcoffee.auth.AuthFragment;
import uol.pagseguro.com.br.smartcoffee.auth.AuthPresenter;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface AuthComponent extends MainComponent {

    AuthPresenter presenter();

    void inject(AuthFragment fragment);
}
