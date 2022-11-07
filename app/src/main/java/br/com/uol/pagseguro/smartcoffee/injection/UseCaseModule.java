package br.com.uol.pagseguro.smartcoffee.injection;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.smartcoffee.payments.PaymentsUseCase;
import br.com.uol.pagseguro.smartcoffee.payments.installments.SelectInstallmentUseCase;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoUseCase;
import br.com.uol.pagseguro.smartcoffee.otherFeatures.softwarecapability.SoftwareCapabilityUseCase;
import br.com.uol.pagseguro.smartcoffee.printer.PrinterUseCase;
import dagger.Module;
import dagger.Provides;
import br.com.uol.pagseguro.smartcoffee.auth.AuthUseCase;
import br.com.uol.pagseguro.smartcoffee.nfc.NFCUseCase;

@Module
public class UseCaseModule {

    @Provides
    AuthUseCase providesAuthUseCase(PlugPag plugPag) {
        return new AuthUseCase(plugPag);
    }

    @Provides
    PaymentsUseCase providesPaymentsUseCase(PlugPag plugPag) {
        return new PaymentsUseCase(plugPag);
    }

    @Provides
    NFCUseCase providesNFCUseCase(PlugPag plugPag) {
        return new NFCUseCase(plugPag);
    }

    @Provides
    PrinterUseCase providesPrinterUseCase(PlugPag plugPag) {
        return new PrinterUseCase(plugPag);
    }

    @Provides
    SelectInstallmentUseCase providesSelectInstallmentUseCase(PlugPag plugPag) {
        return new SelectInstallmentUseCase(plugPag);
    }

    @Provides
    PreAutoUseCase providesPreAutoUseCase(PlugPag plugPag) {
        return new PreAutoUseCase(plugPag);
    }

    @Provides
    SoftwareCapabilityUseCase providesSoftwareCapabilityUseCase(PlugPag plugPag) {
        return new SoftwareCapabilityUseCase(plugPag);
    }
}
