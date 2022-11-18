package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import com.hannesdorfmann.mosby.mvp.MvpView;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;

public interface PreAutoContract extends MvpView {

    void showTransactionSuccess();

    void showTransactionDialog(String message);

    void dismissDialog();

    void showLoading(boolean show);

    void writeToFile(String transactionCode, String transactionId);

    void showDialogValuePreAuto(
        DismissListenerEffectivate onDismissListener,
        PlugPagTransactionResult plugPagTransactionResult
    );

    void showPreAutoDetail(
        PlugPagTransactionResult plugPagTransactionResult,
        Boolean isPreAutoCancel
    );

}