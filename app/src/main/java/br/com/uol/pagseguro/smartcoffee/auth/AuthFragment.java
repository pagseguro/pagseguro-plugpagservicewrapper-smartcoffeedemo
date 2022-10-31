package br.com.uol.pagseguro.smartcoffee.auth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.HomeFragment;
import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.FragmentAuthBinding;
import br.com.uol.pagseguro.smartcoffee.injection.AuthComponent;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerAuthComponent;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;

public class AuthFragment extends MvpFragment<AuthContract, AuthPresenter> implements AuthContract, HomeFragment {

    @Inject
    AuthComponent mInjector;

    public static AuthFragment getInstance() {
        return new AuthFragment();
    }
    private FragmentAuthBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInjector = DaggerAuthComponent.builder()
                .mainComponent(((MainActivity) getContext()).getMainComponent())
                .build();
        mInjector.inject(this);
        binding = FragmentAuthBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickButtons();
    }

    private void clickButtons() {
        binding.btnAuthenticationCheck.setOnClickListener(click -> getPresenter().checkIsAuthenticated());
        binding.btnAuthenticationRequest.setOnClickListener(click -> getPresenter().requestAuth());
        binding.btnAuthenticationInvalidate.setOnClickListener(click -> getPresenter().deactivate());
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
    public void showDeactivatedSuccessfully() {
        UIFeedback.showDialog(getContext(), R.string.auth_deactivated_successfully);
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
