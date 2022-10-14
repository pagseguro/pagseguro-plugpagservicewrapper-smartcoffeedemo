package br.com.uol.pagseguro.smartcoffee.permissions.softwarecapability;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface SoftwareCapabilityContract extends MvpView {

    void showError(String message);

    void showLoading();

    void showSuccess(String message);

}
