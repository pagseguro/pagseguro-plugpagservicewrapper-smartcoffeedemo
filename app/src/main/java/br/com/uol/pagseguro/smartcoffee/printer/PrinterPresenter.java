package br.com.uol.pagseguro.smartcoffee.printer;

import android.util.Log;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import java.io.FileNotFoundException;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PrinterPresenter extends MvpNullObjectBasePresenter<PrinterContract> {

    private final PrinterUseCase mUseCase;

    private Disposable mSubscribe;

    @Inject
    public PrinterPresenter(PrinterUseCase useCase) {
        mUseCase = useCase;
    }

    public void printFile() {
        mSubscribe = mUseCase.printFile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(result -> {
                            if (result.getResult() == 0) {
                                getView().showSucess();
                            } else {
                                getView().showError(result.getMessage());
                            }
                        },
                        throwable -> {
                            if (throwable instanceof FileNotFoundException) {
                                getView().showFileNotFound();
                            } else {
                                getView().showError(throwable.getMessage());
                            }
                        });
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
    }
}
