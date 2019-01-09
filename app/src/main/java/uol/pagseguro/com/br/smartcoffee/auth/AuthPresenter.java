package uol.pagseguro.com.br.smartcoffee.auth;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AuthPresenter extends MvpNullObjectBasePresenter<AuthContract> {

    private final AuthUseCase mUseCase;

    private Disposable mSubscribe;

    @Inject
    public AuthPresenter(AuthUseCase useCase) {
        mUseCase = useCase;
    }

    public void checkIsAuthenticated() {
        mSubscribe = mUseCase.isAuthenticated()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isAuthenticated -> getView().showIsAuthenticated(isAuthenticated),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void requestAuth() {
        mSubscribe = mUseCase.initializeAndActivatePinpad()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> getView().showActivatedSuccessfully(),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void invalidateAuth() {
        mSubscribe = mUseCase.invalidateAuthentication()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> getView().showInvalidatedSuccessfully());
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
        super.detachView(retainInstance);
    }
}
