package br.com.uol.pagseguro.smartcoffee.payments.installments;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpPresenter;

import java.util.List;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInstallment;
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

    public void calculateInstallments(Integer saleValue, Integer installmentType) {
        mSubscribe = mUseCase.calculateInstallments(saleValue, installmentType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(disposable -> getView().showLoading(true))
            .doFinally(() -> getView().showLoading(false))
            .subscribe(
                installments ->
                    getView().setUpAdapter(installments),
                throwable -> getView().showMessage(throwable.getMessage()));
    }
}
