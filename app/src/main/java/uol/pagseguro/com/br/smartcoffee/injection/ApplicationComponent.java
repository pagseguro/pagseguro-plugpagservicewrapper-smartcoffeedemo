package uol.pagseguro.com.br.smartcoffee.injection;

import android.content.Context;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import dagger.Component;

@Component(modules = {WrapperModule.class})
public interface ApplicationComponent {

    PlugPag plugPag();
}
