package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import android.support.annotation.Nullable;

import java.util.Locale;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEffectuatePreAutoData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoKeyingData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoQueryData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class PreAutoUseCase {

    private final PlugPag mPlugPag;

    public PreAutoUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<ActionResult> doPreAutoCreate(
            int value,
            int installmentType,
            int installments,
            @Nullable String securityCode,
            @Nullable String expirationDate,
            @Nullable String pan
    ) {
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setListener(emitter, result, null);
            setPrintListener(emitter, result);

            PlugPagTransactionResult plugPagTransactionResult;

            if (securityCode != null && expirationDate != null && pan != null)
                plugPagTransactionResult = mPlugPag.doPreAutoCreate(
                    new PlugPagPreAutoKeyingData(
                        value,
                        installmentType,
                        installments,
                        "pagcafe",
                        true,
                        pan,
                        securityCode,
                        expirationDate
                    )
                );
            else
                plugPagTransactionResult = mPlugPag.doPreAutoCreate(
                    new PlugPagPreAutoData(
                        value,
                        installmentType,
                        installments,
                        "pagcafe",
                        true
                    )
                );

            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    public Observable<ActionResult> doPreAutoEffectuate(
        int value,
        String transactionId,
        String transactionCode
    ) {
        return Observable.create(emitter -> {
            PlugPagEffectuatePreAutoData plugPagEffectuatePreAutoData = new PlugPagEffectuatePreAutoData(
                value,
                null,
                true,
                transactionId,
                transactionCode
            );

            ActionResult result = new ActionResult();
            setListener(emitter, result, null);
            setPrintListener(emitter, result);
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.doEffectuatePreAuto(plugPagEffectuatePreAutoData);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    public Observable<ActionResult> doPreAutoCancel(String transactionId, String transactionCode) {
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setListener(emitter, result, null);
            setPrintListener(emitter, result);
            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.doPreAutoCancel(transactionId, transactionCode);
            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    public Observable<ActionResult> getPreAutoData(@Nullable PlugPagPreAutoQueryData preAutoQueryData) {
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setListener(emitter, result, null);

            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.getPreAutoData(preAutoQueryData);

            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    public Observable<ActionResult> getPreAutoHistory() {
        return Observable.create(emitter -> {
            ActionResult result = new ActionResult();
            setListener(emitter, result, null);

            PlugPagTransactionResult plugPagTransactionResult = mPlugPag.getPreAutoData(null);

            sendResponse(emitter, plugPagTransactionResult, result);
        });
    }

    public Observable<ActionResult> initializeAndActivatePinpad(String activationCode) {
        return Observable.create(emitter -> {
            ActionResult actionResult = new ActionResult();
            mPlugPag.setEventListener(plugPagEventData -> {
                actionResult.setEventCode(plugPagEventData.getEventCode());
                actionResult.setMessage(plugPagEventData.getCustomMessage());
                emitter.onNext(actionResult);
            });

            PlugPagInitializationResult result = mPlugPag.initializeAndActivatePinpad(new PlugPagActivationData(activationCode));

            if (result.getResult() == PlugPag.RET_OK) {
                emitter.onNext(new ActionResult());
            } else {
                emitter.onError(new RuntimeException(result.getErrorMessage()));
            }

            emitter.onComplete();
        });
    }

    public Completable abort() {
        return Completable.create(emitter -> mPlugPag.abort());
    }

    public Observable<Boolean> isAuthenticated() {
        return Observable.create(emitter -> {
            emitter.onNext(mPlugPag.isAuthenticated());
            emitter.onComplete();
        });
    }

    private void setPrintListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setPrinterListener(new PlugPagPrinterListener() {
            @Override
            public void onError(PlugPagPrintResult printResult) {
                result.setResult(printResult.getResult());
                result.setMessage(String.format("Error %s %s", printResult.getErrorCode(), printResult.getMessage()));
                result.setErrorCode(printResult.getErrorCode());
                emitter.onNext(result);
            }

            @Override
            public void onSuccess(PlugPagPrintResult printResult) {
                result.setResult(printResult.getResult());
                result.setMessage(String.format(Locale.getDefault(), "Print OK: Steps [%d]", printResult.getSteps()));
                result.setErrorCode(printResult.getErrorCode());
                emitter.onNext(result);
            }
        });
    }

    private void setListener(
        ObservableEmitter<ActionResult> emitter,
        ActionResult result,
        PlugPagTransactionResult plugPagTransactionResult
    ) {
        mPlugPag.setEventListener(plugPagEventData -> {
            result.setEventCode(plugPagEventData.getEventCode());
            result.setMessage(plugPagEventData.getCustomMessage());
            result.setTransactionResult(plugPagTransactionResult);
            emitter.onNext(result);
        });
    }

    private void sendResponse(
        ObservableEmitter<ActionResult> emitter,
        PlugPagTransactionResult plugPagTransactionResult,
        ActionResult result
    ) {
        if (plugPagTransactionResult != null &&
            plugPagTransactionResult.getResult() != null &&
            plugPagTransactionResult.getResult() != 0
        ) {
            emitter.onError(new PlugPagException(plugPagTransactionResult.getMessage(), plugPagTransactionResult.getErrorCode()));
        } else {
            result.setTransactionCode(plugPagTransactionResult.getTransactionCode());
            result.setTransactionId(plugPagTransactionResult.getTransactionId());
            result.setTransactionResult(plugPagTransactionResult);
            emitter.onNext(result);
        }

        emitter.onComplete();
    }
}
