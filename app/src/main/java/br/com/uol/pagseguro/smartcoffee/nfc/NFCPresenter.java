package br.com.uol.pagseguro.smartcoffee.nfc;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NFCPresenter extends MvpNullObjectBasePresenter<NFCContract> {

    private final NFCUseCase mUseCase;

    private Disposable mSubscribe;

    @Inject
    public NFCPresenter(NFCUseCase useCase) {
        mUseCase = useCase;
    }

    public void readNFCCard() {
        mSubscribe = mUseCase.readNFCCard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> getView().showSuccess(result),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    public void writeNFCCard() {
        mSubscribe = mUseCase.writeNFCCard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> getView().showSuccess(result),
                        throwable -> getView().showError(throwable.getMessage()));
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
    }

    public void abort() {
        mSubscribe = mUseCase.abort()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
