package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.payments.qrcode.QrcodeActivity;
import br.com.uol.pagseguro.smartcoffee.payments.qrcode.QrcodePresenter;
import dagger.Component;

@Component(modules = {UseCaseModule.class, WrapperModule.class})
public interface QrcodeComponent {
    void inject(QrcodeActivity activity);

    QrcodePresenter presenter();
}
