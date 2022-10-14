package br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import java.util.ArrayList;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCommand;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SoftwareCapabilityPresenter extends MvpNullObjectBasePresenter<SoftwareCapabilityContract> {

    private SoftwareCapabilityUseCase mUseCase;
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
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_REPRINT_CUSTOMER_RECEIPT, "REPRINT_CUSTOMER_RECEIPT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_INVALIDATE_AUTHENTICATION, "INVALIDATE_AUTHENTICATION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_CALCULATE_INSTALLMENTS, "CALCULATE_INSTALLMENTS"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_CHECK_AUTHENTICATION, "CHECK_AUTHENTICATION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_GET_LIB_VERSION, "GET_LIB_VERSION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_GET_APPLICATION_CODE, "GET_APPLICATION_CODE"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_ABORT, "ABORT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_REPRINT_STABLISHMENT_RECEIPT, "REPRINT_STABLISHMENT_RECEIPT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_PRINT, "PRINT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_QUERY_LAST_APPROVED_TRANSACTION, "QUERY_LAST_APPROVED_TRANSACTION"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_ENABLE_MOCK, "ENABLE_MOCK"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_CHECK_MOCK_STATE, "CHECK_MOCK_STATE"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_SET_MOCK_RESULT, "SET_MOCK_RESULT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_GET_MOCK_RESULT, "GET_MOCK_RESULT"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_SEND_SMS, "SEND_SMS"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_GET_CARD_DATA, "GET_CARD_DATA"));
//        softwareCapabilities.add(new SoftwareCapability(PlugPag.OPERATION_SET_PROFILE, "SET_PROFILE"));
    }

    public void loadCapabilities() {
        prepareList();

        getView().showLoading();

        loadCapabilities(0);
    }

    private void loadCapabilities(int index) {
        if (index >= softwareCapabilities.size()) {
            getView().showSuccess(testedMessage());
            return;
        }

        final SoftwareCapability softwareCapability = softwareCapabilities.get(index);
        if (softwareCapability.hasParameter()) {
            mSubscribes.add(
                    mUseCase.loadSoftwareCapability(softwareCapability.getIndex(), softwareCapability.getMode())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .doOnComplete(() -> loadCapabilities(index + 1))
                            .doOnError(throwable -> getView().showError(throwable.getMessage()))
                            .subscribe(message -> softwareCapability.setHas(message),
                                    throwable -> getView().showError(throwable.getMessage()))
            );
        } else {
            mSubscribes.add(
                    mUseCase.loadSoftwareCapability(softwareCapability.getIndex())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .doOnComplete(() -> loadCapabilities(index + 1))
                            .doOnError(throwable -> getView().showError(throwable.getMessage()))
                            .subscribe(message -> softwareCapability.setHas(message),
                                    throwable -> getView().showError(throwable.getMessage()))
            );
        }
    }

    private String testedMessage() {
        String message = "";

        for (SoftwareCapability softwareCapability : softwareCapabilities) {
            message += softwareCapability.getMessage();
        }

        return message;
    }
}
