package uol.pagseguro.com.br.smartcoffee.auth;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface AuthContract extends MvpView {

    void showIsAuthenticated(Boolean isAuthenticated);

    void showError(String message);

    void showActivatedSuccessfully();

    void showInvalidatedSuccessfully();
}
