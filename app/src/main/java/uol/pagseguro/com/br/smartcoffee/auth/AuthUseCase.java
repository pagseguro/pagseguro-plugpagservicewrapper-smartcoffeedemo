package uol.pagseguro.com.br.smartcoffee.auth;


import java.util.concurrent.Callable;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

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

    public Observable<Object> initializeAndActivatePinpad() {
        return Observable.create(emitter -> {
            PlugPagInitializationResult result = mPlugPag.initializeAndActivatePinpad(new PlugPagActivationData("403938"));
            if(result.getResult() == PlugPag.RET_OK) {
                emitter.onNext(new Object());
            } else {
                emitter.onError(new RuntimeException(result.getErrorMessage()));
            }
            emitter.onComplete();
        });
    }

    public Completable invalidateAuthentication() {
        return Completable.defer(() -> {
            mPlugPag.invalidateAuthentication();
            return Completable.complete();
        });
    }
}
