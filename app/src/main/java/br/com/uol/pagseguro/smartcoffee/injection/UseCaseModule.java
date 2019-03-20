package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import dagger.Module;
import dagger.Provides;
import br.com.uol.pagseguro.smartcoffee.auth.AuthUseCase;
import br.com.uol.pagseguro.smartcoffee.transactions.TransactionsUseCase;
import br.com.uol.pagseguro.smartcoffee.nfc.NFCUseCase;

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

    @Provides
    NFCUseCase providesNFCUseCase(PlugPag plugPag) {
        return new NFCUseCase(plugPag);
    }
}
