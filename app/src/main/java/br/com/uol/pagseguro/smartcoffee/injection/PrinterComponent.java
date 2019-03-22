package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.smartcoffee.printer.PrinterFragment;
import br.com.uol.pagseguro.smartcoffee.printer.PrinterPresenter;
import dagger.Component;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface PrinterComponent {

    PrinterPresenter presenter();

    void inject(PrinterFragment fragment);
}
