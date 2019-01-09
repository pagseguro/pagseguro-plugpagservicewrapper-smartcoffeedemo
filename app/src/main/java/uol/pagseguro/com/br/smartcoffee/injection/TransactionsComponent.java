package uol.pagseguro.com.br.smartcoffee.injection;

import dagger.Component;
import uol.pagseguro.com.br.smartcoffee.transactions.TransactionsFragment;
import uol.pagseguro.com.br.smartcoffee.transactions.TransactionsPresenter;

@Component(dependencies = {MainComponent.class}, modules = {UseCaseModule.class})
public interface TransactionsComponent extends MainComponent {

    TransactionsPresenter presenter();

    void inject(TransactionsFragment fragment);
}
