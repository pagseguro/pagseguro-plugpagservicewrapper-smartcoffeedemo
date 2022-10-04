package br.com.uol.pagseguro.smartcoffee.payments.installments;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpPresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SelectInstallmentPresenter extends MvpNullObjectBasePresenter<SelectInstallmentContract>
    implements MvpPresenter<SelectInstallmentContract> {

    private final SelectInstallmentUseCase mUseCase;
    private Disposable mSubscribe;

    @Inject
    public SelectInstallmentPresenter(SelectInstallmentUseCase useCase) {
        mUseCase = useCase;
    }

    @Override
    public void detachView(boolean retainInstance) {
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
        super.detachView(retainInstance);
    }

    public void calculateInstallments(Integer saleValue, Integer installmentType, Boolean isPreAutoKeyed) {
        mSubscribe = mUseCase.isAuthenticated()
            .filter(isAuthenticated -> {
                if (!isAuthenticated) {
                    getView().showActivationDialog();
                    mSubscribe.dispose();
                }

                return isAuthenticated;
            })
            .flatMap((Function<Boolean, ObservableSource<List<String>>>) aBoolean ->
                    mUseCase.calculateInstallments(
                            saleValue,
                            installmentType,
                            isPreAutoKeyed
                    ))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposable -> getView().showLoading(true))
            .doOnComplete(() -> getView().showLoading(false))
            .subscribe(
                installments ->
                    getView().setUpAdapter(installments),
                throwable -> {
                    getView().showLoading(false);
                    getView().showError(throwable.getMessage());
                });
    }

    public void activate(String activationCode) {
        mSubscribe = mUseCase.initializeAndActivatePinpad(activationCode)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> getView().showLoading(true))
            .doOnComplete(() -> getView().showLoading(false))
            .subscribe(
                actionResult ->
                    getView().showAuthProgress(actionResult.getMessage()),
                throwable -> {
                    getView().showLoading(false);
                    getView().showError(throwable.getMessage());
                });
    }
}
