package br.com.uol.pagseguro.smartcoffee.auth;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doOnComplete(() -> getView().showLoading(false))
                .subscribe(isAuthenticated -> getView().showIsAuthenticated(isAuthenticated),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void requestAuth(String activationCode) {
        mSubscribe = mUseCase.initializeAndActivatePinpad(activationCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doOnTerminate(() -> getView().showLoading(false))
                .subscribe(object -> getView().showActivatedSuccessfully(),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void deactivate(String activationCode) {
        mSubscribe = mUseCase.deactivate(activationCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doOnTerminate(() -> getView().showLoading(false))
                .subscribe(object -> getView().showDeactivatedSuccessfully(),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
        super.detachView(retainInstance);
    }
}
