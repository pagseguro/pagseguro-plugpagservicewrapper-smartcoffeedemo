package br.com.uol.pagseguro.smartcoffee.injection;

import android.content.Context;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;
import dagger.Module;
import dagger.Provides;

@Module
public class WrapperModule {

    private final Context mContext;

    public WrapperModule(Context context) {
        mContext = context;
    }

    @Provides
    PlugPag providesPlugPag() {
        PlugPag plugPag = new PlugPag(mContext,new PlugPagAppIdentification("TESTE", "1"));
        return plugPag;
    }
}
