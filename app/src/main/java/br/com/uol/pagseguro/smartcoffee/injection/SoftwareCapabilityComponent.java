package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.permissions.PermissionsFragment;
import br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability.SoftwareCapabilityPresenter;
import dagger.Component;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface SoftwareCapabilityComponent extends MainComponent {

    SoftwareCapabilityPresenter presenter();

    void inject(PermissionsFragment fragment);
}
