package br.com.uol.pagseguro.smartcoffee.injection;

import dagger.Module;
import dagger.Provides;
import br.com.uol.pagseguro.smartcoffee.utils.FragmentFlowManager;

@Module
public class ScreenFlowModule {

    @Provides
    FragmentFlowManager providesFragmentFlowManager() {
        return new FragmentFlowManager();
    }

}
