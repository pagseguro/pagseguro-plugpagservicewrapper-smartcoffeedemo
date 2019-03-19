package uol.pagseguro.com.br.smartcoffee.injection;

import dagger.Component;
import uol.pagseguro.com.br.smartcoffee.nfc.NFCFragment;
import uol.pagseguro.com.br.smartcoffee.nfc.NFCPresenter;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface NFCComponent {

    void inject(NFCFragment fragment);

    NFCPresenter presenter();
}
