package uol.pagseguro.com.br.smartcoffee.injection;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import dagger.Component;
import uol.pagseguro.com.br.smartcoffee.MainActivity;
import uol.pagseguro.com.br.smartcoffee.utils.FragmentFlowManager;

@Component(modules = {WrapperModule.class, ScreenFlowModule.class})
public interface MainComponent {

    void inject(MainActivity activity);

    PlugPag plugPag();

    FragmentFlowManager flowManager();
}
