package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.ApplicationSelectorActivity;
import dagger.Component;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface ApplicationSelectorComponent {

    void inject(ApplicationSelectorActivity fragment);

}
