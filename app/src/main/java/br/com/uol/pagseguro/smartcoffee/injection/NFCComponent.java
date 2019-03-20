package br.com.uol.pagseguro.smartcoffee.injection;

import dagger.Component;
import br.com.uol.pagseguro.smartcoffee.nfc.NFCFragment;
import br.com.uol.pagseguro.smartcoffee.nfc.NFCPresenter;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface NFCComponent {

    void inject(NFCFragment fragment);

    NFCPresenter presenter();
}
