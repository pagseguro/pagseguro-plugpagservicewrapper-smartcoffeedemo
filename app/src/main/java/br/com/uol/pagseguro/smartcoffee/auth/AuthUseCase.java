package br.com.uol.pagseguro.smartcoffee.auth;


import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.Observable;

public class AuthUseCase {

    private final PlugPag mPlugPag;

    public AuthUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<Boolean> isAuthenticated() {
        return Observable.create(emitter -> {
            emitter.onNext(mPlugPag.isAuthenticated());
            emitter.onComplete();
        });
    }

    public Observable<Object> initializeAndActivatePinpad(String activationCode) {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            mPlugPag.setEventListener(plugPagEventData -> {
                actionResult.setEventCode(plugPagEventData.getEventCode());
                actionResult.setMessage(plugPagEventData.getCustomMessage());
                emitter.onNext(actionResult);
            });

            PlugPagInitializationResult result =
                    mPlugPag.initializeAndActivatePinpad(
                            new PlugPagActivationData(activationCode)
                    );
            if (result.getResult() == PlugPag.RET_OK) {
                emitter.onNext(new ActionResult());
            } else {
                emitter.onError(new RuntimeException(result.getErrorMessage()));
            }
            emitter.onComplete();
        });
    }

    public Observable<Object> deactivate(String activationCode) {
        return Observable.create(emitter -> {
            PlugPagInitializationResult result =
                    mPlugPag.deactivate(
                            new PlugPagActivationData(activationCode)
                    );
            if (result.getResult() == PlugPag.RET_OK) {
                emitter.onNext(new Object());
            } else {
                emitter.onError(new RuntimeException(result.getErrorMessage()));
            }
            emitter.onComplete();
        });
    }
}
