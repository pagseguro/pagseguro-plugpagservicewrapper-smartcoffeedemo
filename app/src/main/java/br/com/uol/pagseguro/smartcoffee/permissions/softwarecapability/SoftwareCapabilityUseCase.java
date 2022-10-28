package br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCommand;
import io.reactivex.Observable;
import io.reactivex.Single;

public class SoftwareCapabilityUseCase {

    private final PlugPag mPlugPag;

    public SoftwareCapabilityUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<Boolean> loadSoftwareCapability(int capability) {
        return loadSoftwareCapability(
                capability,
                PlugPagCommand.OPERATION_NO_EXTRA_PARAMS.getCommand()
        );
    }

    public Observable<Boolean> loadSoftwareCapability(int capability, int mode) {
        return Observable.create(emitter -> {
            boolean hasCapability = mPlugPag.hasSoftwareCapability(capability, mode);
            emitter.onNext(hasCapability);
            emitter.onComplete();
        });
    }

    public Single<String> showProductInitialization() {
        return Single.create(emitter -> {

            String message = "Pré-autorização: " +
                    mPlugPag.hasSoftwareCapability(
                            PlugPagCommand.OPERATION_REMOTECFG_PRE_AUTORIZACAO_CARTAO.getCommand(),
                            PlugPagCommand.OPERATION_MODE_REMOTECFG.getCommand()
                    ) +
                    "\nPré-autorização digitada: " +
                    mPlugPag.hasSoftwareCapability(
                            PlugPagCommand.OPERATION_REMOTECFG_PRE_AUTORIZACAO_DIGITADA.getCommand(),
                            PlugPagCommand.OPERATION_MODE_REMOTECFG.getCommand()
                    ) +
                    "\nPagamento Carnê: " +
                    mPlugPag.hasSoftwareCapability(
                            PlugPagCommand.OPERATION_REMOTECFG_PAGAMENTO_CARNE.getCommand(),
                            PlugPagCommand.OPERATION_MODE_REMOTECFG.getCommand()
                    );

            emitter.onSuccess(message);
        });
    }
}
