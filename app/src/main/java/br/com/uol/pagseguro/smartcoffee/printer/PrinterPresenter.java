package br.com.uol.pagseguro.smartcoffee.printer;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
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
                .doOnComplete(() -> getView().showLoading(false))
                .subscribe(new Consumer<ActionResult>() {
                               @Override
                               public void accept(ActionResult actionResult) {

                                   getView().showError(String.format("Error %s %s", actionResult.getResult(), actionResult.getMessage()));
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
