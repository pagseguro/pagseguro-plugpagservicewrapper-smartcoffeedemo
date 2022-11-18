package br.com.uol.pagseguro.smartcoffee.injection;

import dagger.Component;
import br.com.uol.pagseguro.smartcoffee.payments.transactions.TransactionsFragment;
import br.com.uol.pagseguro.smartcoffee.payments.transactions.TransactionsPresenter;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface TransactionsComponent extends MainComponent {

    TransactionsPresenter presenter();

    void inject(TransactionsFragment fragment);
}
