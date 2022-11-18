package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.otherFeatures.OtherFeaturesFragment;
import br.com.uol.pagseguro.smartcoffee.otherFeatures.softwarecapability.SoftwareCapabilityPresenter;
import dagger.Component;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface SoftwareCapabilityComponent extends MainComponent {

    SoftwareCapabilityPresenter presenter();

    void inject(OtherFeaturesFragment fragment);
}
