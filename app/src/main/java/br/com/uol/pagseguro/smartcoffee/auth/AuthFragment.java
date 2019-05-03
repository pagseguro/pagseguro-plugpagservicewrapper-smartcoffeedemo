package br.com.uol.pagseguro.smartcoffee.auth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.HomeFragment;
import butterknife.ButterKnife;
import butterknife.OnClick;
import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.injection.AuthComponent;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerAuthComponent;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;

public class AuthFragment extends MvpFragment<AuthContract, AuthPresenter> implements AuthContract, HomeFragment {

    @Inject
    AuthComponent mInjector;

    public static AuthFragment getInstance() {
        return new AuthFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInjector = DaggerAuthComponent.builder()
                .mainComponent(((MainActivity) getContext()).getMainComponent())
                .build();
        mInjector.inject(this);
        View rootView = inflater.inflate(R.layout.fragment_auth, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick(R.id.btn_authentication_check)
    public void onCheckAuthClicked() {
        getPresenter().checkIsAuthenticated();
    }

    @OnClick(R.id.btn_authentication_request)
    public void onRequestAuthClicked() {
        getPresenter().requestAuth();
    }

    @OnClick(R.id.btn_authentication_invalidate)
    public void onInvalidateAuthClicked() {
        getPresenter().invalidateAuth();
    }

    @Override
    public AuthPresenter createPresenter() {
        return mInjector.presenter();
    }

    @Override
    public void showIsAuthenticated(Boolean isAuthenticated) {
        UIFeedback.showDialog(getContext(), isAuthenticated ?
                R.string.auth_is_authenticated : R.string.auth_isnt_authenticated);
    }

    @Override
    public void showError(String message) {
        UIFeedback.showDialog(getContext(), message);
    }

    @Override
    public void showActivatedSuccessfully() {
        UIFeedback.showDialog(getContext(), R.string.auth_activated_successfully);
    }

    @Override
    public void showInvalidatedSuccessfully() {
        UIFeedback.showDialog(getContext(), R.string.auth_invalidated_successfully);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            UIFeedback.showProgress(getContext());
        } else {
            UIFeedback.dismissProgress();
        }
    }
}
