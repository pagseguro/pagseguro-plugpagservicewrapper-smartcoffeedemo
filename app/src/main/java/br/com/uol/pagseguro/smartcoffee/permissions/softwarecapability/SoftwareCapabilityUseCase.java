package br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCommand;
import io.reactivex.Observable;

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
}
