package br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface SoftwareCapabilityContract extends MvpView {

    void showLoading(boolean show);

    void showDialog(String message);
}
