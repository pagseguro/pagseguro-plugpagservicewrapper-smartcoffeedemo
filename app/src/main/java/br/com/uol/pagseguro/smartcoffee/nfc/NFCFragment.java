package br.com.uol.pagseguro.smartcoffee.nfc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.smartcoffee.HomeFragment;
import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.databinding.FragmentNfcBinding;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerNFCComponent;
import br.com.uol.pagseguro.smartcoffee.injection.NFCComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;

public class NFCFragment extends MvpFragment<NFCContract, NFCPresenter> implements NFCContract, HomeFragment {

    NFCComponent mInjector;

    public static NFCFragment getInstance() {
        return new NFCFragment();
    }
    private FragmentNfcBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInjector = DaggerNFCComponent.builder()
                .useCaseModule(new UseCaseModule())
                .mainComponent(((MainActivity) getContext()).getMainComponent())
                .build();
        mInjector.inject(this);
        binding = FragmentNfcBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickButtons();
    }

    @Override
    public NFCPresenter createPresenter() {
        return mInjector.presenter();
    }

    private void clickButtons() {
        binding.btnNfcRead.setOnClickListener(click ->
                getPresenter().readNFCCard()
        );
        binding.btnNfcWrite.setOnClickListener(click ->
                getPresenter().writeNFCCard()
        );
        binding.btnNfcWriteDirectly.setOnClickListener(click ->
                getPresenter().writeDirectlyNFCCard()
        );
        binding.btnDetectCardDirectly.setOnClickListener(click ->
                getPresenter().detectCardDirectly()
        );
        binding.btnDetectRemoveCardDirectly.setOnClickListener(click ->
                getPresenter().detectRemoveCardDirectly()
        );
        binding.btnApduCmdExchange.setOnClickListener(click ->
                getPresenter().cmdExchange()
        );
        binding.btnAuthDirectly.setOnClickListener(click ->
                getPresenter().detectJustAuthDirectly()
        );
        binding.btnAuthNfcBlocoB.setOnClickListener(click ->
                getPresenter().authNfcBlocoBDirectly()
        );
        binding.btnNfcAbort.setOnClickListener(click ->
                getPresenter().abort()
        );
        binding.btnNfcBeep.setOnClickListener(click ->
                getPresenter().beepNfc()
        );
        binding.btnBlueLedNfc.setOnClickListener(click ->
                getPresenter().setLedBlueNFC()
        );
        binding.btnYellowLedNfc.setOnClickListener(click ->
                getPresenter().setLedYellowNFC()
        );
        binding.btnGreenLedNfc.setOnClickListener(click ->
                getPresenter().setLedGreenNFC()
        );
        binding.btnRedLedNfc.setOnClickListener(click ->
                getPresenter().setLedRedNFC()
        );
        binding.btnOffLedNfc.setOnClickListener(click ->
                getPresenter().setLedOffNFC()
        );
    }

    @Override
    public void showDialog(String message) {
        UIFeedback.showDialog(getContext(), message);
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
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
