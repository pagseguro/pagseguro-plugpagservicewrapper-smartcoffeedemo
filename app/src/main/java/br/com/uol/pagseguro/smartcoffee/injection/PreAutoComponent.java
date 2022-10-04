package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoPresenter;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.detail.PreAutoDetailActivity;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.detail.PreAutoDetailPresenter;
import dagger.Component;

@Component(
    modules = {
        UseCaseModule.class,
        WrapperModule.class
    }
)
public interface PreAutoComponent {

    void inject(PreAutoActivity activity);
    void inject(PreAutoDetailActivity preAutoDetailActivity);

    PreAutoPresenter presenter();
    PreAutoDetailPresenter preAutoDetailPresenter();
}
