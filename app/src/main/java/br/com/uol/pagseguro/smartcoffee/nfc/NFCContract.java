package br.com.uol.pagseguro.smartcoffee.nfc;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface NFCContract extends MvpView {
    void showSnackbar(String message);

    void showDialog(String message);

    void showLoading(boolean show);
}
