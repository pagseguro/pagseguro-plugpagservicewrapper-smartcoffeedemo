package br.com.uol.pagseguro.smartcoffee.printer;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface PrinterContract extends MvpView{

    void showSucess();

    void showError(String message);

    void showLoading(boolean show);

    void showFileNotFound();
}
