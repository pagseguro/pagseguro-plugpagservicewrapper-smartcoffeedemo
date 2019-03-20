package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import dagger.Component;
import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.utils.FragmentFlowManager;

@Component(modules = {WrapperModule.class, ScreenFlowModule.class})
public interface MainComponent {

    void inject(MainActivity activity);

    PlugPag plugPag();

    FragmentFlowManager flowManager();
}
