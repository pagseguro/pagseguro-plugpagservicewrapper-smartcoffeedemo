package uol.pagseguro.com.br.smartcoffee.injection;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import dagger.Module;
import dagger.Provides;
import uol.pagseguro.com.br.smartcoffee.auth.AuthUseCase;
import uol.pagseguro.com.br.smartcoffee.transactions.TransactionsUseCase;

@Module
public class UseCaseModule {

    @Provides
    AuthUseCase providesAuthUseCase(PlugPag plugPag) {
        return new AuthUseCase(plugPag);
    }

    @Provides
    TransactionsUseCase providesTransactionsUseCase(PlugPag plugPag) {
        return new TransactionsUseCase(plugPag);
    }
}
