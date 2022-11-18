package br.com.uol.pagseguro.smartcoffee.otherFeatures.softwarecapability;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import java.util.ArrayList;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCommand;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SoftwareCapabilityPresenter extends MvpNullObjectBasePresenter<SoftwareCapabilityContract> {

    private final SoftwareCapabilityUseCase mUseCase;
    private CompositeDisposable mSubscribes;

    private ArrayList<SoftwareCapability> softwareCapabilities;

    @Inject
    public SoftwareCapabilityPresenter(SoftwareCapabilityUseCase useCase) {
        mUseCase = useCase;
    }

    @Override
    public void attachView(SoftwareCapabilityContract view) {
        super.attachView(view);

        mSubscribes = new CompositeDisposable();
    }

    @Override
    public void detachView(boolean retainInstance) {
        mSubscribes.clear();

        super.detachView(retainInstance);
    }

    private void prepareList() {
        if (softwareCapabilities != null) {
            for (SoftwareCapability softwareCapability : softwareCapabilities)
                softwareCapability.setHas(false);
            return;
        }

        softwareCapabilities = new ArrayList<>();
        softwareCapabilities.add(new SoftwareCapability(100, "FAKE 1"));
        softwareCapabilities.add(new SoftwareCapability(1000, "FAKE 2"));

        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_PAYMENT.getCommand(), PlugPag.TYPE_CREDITO, "PAYMENT : TYPE_CREDITO"));
        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_PAYMENT.getCommand(), PlugPag.TYPE_DEBITO, "PAYMENT : TYPE_DEBITO"));
        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_PAYMENT.getCommand(), PlugPag.TYPE_QRCODE, "PAYMENT : TYPE_QRCODE"));
        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_PAYMENT.getCommand(), PlugPag.TYPE_VOUCHER, "PAYMENT : TYPE_VOUCHER"));

        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_REFUND.getCommand(), PlugPag.VOID_PAYMENT, "VOID : VOID_PAYMENT"));
        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_REFUND.getCommand(), PlugPag.VOID_QRCODE, "VOID : VOID_QRCODE"));

        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_ACTIVATE.getCommand(), "ACTIVATE"));
        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_DEACTIVATE.getCommand(), "DEACTIVATE"));
        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_GET_READER_INFOS.getCommand(), "GET_READER_INFOS"));

        // Keep just a sample
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_REPRINT_CUSTOMER_RECEIPT.getCommand(), "REPRINT_CUSTOMER_RECEIPT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_INVALIDATE_AUTHENTICATION.getCommand(), "INVALIDATE_AUTHENTICATION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_CALCULATE_INSTALLMENTS.getCommand(), "CALCULATE_INSTALLMENTS"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_CHECK_AUTHENTICATION.getCommand(), "CHECK_AUTHENTICATION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_GET_LIB_VERSION.getCommand(), "GET_LIB_VERSION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_GET_APPLICATION_CODE, "GET_APPLICATION_CODE"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_ABORT.getCommand(), "ABORT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_REPRINT_STABLISHMENT_RECEIPT, "REPRINT_STABLISHMENT_RECEIPT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_PRINT.getCommand(), "PRINT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_QUERY_LAST_APPROVED_TRANSACTION.getCommand(), "QUERY_LAST_APPROVED_TRANSACTION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_ENABLE_MOCK, "ENABLE_MOCK"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_CHECK_MOCK_STATE, "CHECK_MOCK_STATE"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_SET_MOCK_RESULT, "SET_MOCK_RESULT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_GET_MOCK_RESULT, "GET_MOCK_RESULT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_SEND_SMS.getCommand(), "SEND_SMS"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_GET_CARD_DATA.getCommand(), "GET_CARD_DATA"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPagCommand.OPERATION_SET_PROFILE.getCommand(), "SET_PROFILE"));
    }

    public void loadCapabilities() {
        prepareList();

        getView().showLoading(true);

        loadCapabilities(0);
    }

    private void loadCapabilities(int index) {
        if (index >= softwareCapabilities.size()) {
            getView().showDialog(testedMessage());
            return;
        }

        final SoftwareCapability softwareCapability = softwareCapabilities.get(index);
        if (softwareCapability.hasParameter()) {
            mSubscribes.add(
                    mUseCase.loadSoftwareCapability(softwareCapability.getIndex(), softwareCapability.getMode())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .doFinally(() -> getView().showLoading(false))
                            .doOnComplete(() -> loadCapabilities(index + 1))
                            .doOnError(throwable -> getView().showDialog(throwable.getMessage()))
                            .subscribe(softwareCapability::setHas,
                                    throwable -> getView().showDialog(throwable.getMessage())
                            )
            );
        } else {
            mSubscribes.add(
                    mUseCase.loadSoftwareCapability(softwareCapability.getIndex())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .doFinally(() -> getView().showLoading(false))
                            .doOnComplete(() -> loadCapabilities(index + 1))
                            .doOnError(throwable -> getView().showDialog(throwable.getMessage()))
                            .subscribe(softwareCapability::setHas,
                                    throwable -> getView().showDialog(throwable.getMessage()))
            );
        }
    }

    private String testedMessage() {
        StringBuilder message = new StringBuilder();

        for (SoftwareCapability softwareCapability : softwareCapabilities) {
            message.append(softwareCapability.getMessage());
        }

        return message.toString();
    }

    public void doProductInitialization() {
        mSubscribes.add(mUseCase.showProductInitialization()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .doOnSuccess(message -> getView().showDialog(message))
                .subscribe()
        );
    }

    public void doReboot() {
        mSubscribes.add(mUseCase.reboot()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> getView().showLoading(false))
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doOnError(throwable -> getView().showDialog(throwable.getMessage()))
                .subscribe(() -> getView().showDialog("O terminal será reiniciado!"), //todo
                        throwable -> getView().showDialog(throwable.getMessage())
                )
        );
    }

    public void doStartOnboarding() {
        mSubscribes.add(mUseCase.startOnboarding()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doOnComplete(() -> getView().showLoading(false))
                .subscribe()
        );
    }
}
