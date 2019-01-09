package uol.pagseguro.com.br.smartcoffee.injection;

import dagger.Module;
import dagger.Provides;
import uol.pagseguro.com.br.smartcoffee.utils.FragmentFlowManager;

@Module
public class ScreenFlowModule {

    @Provides
    FragmentFlowManager providesFragmentFlowManager() {
        return new FragmentFlowManager();
    }

}
