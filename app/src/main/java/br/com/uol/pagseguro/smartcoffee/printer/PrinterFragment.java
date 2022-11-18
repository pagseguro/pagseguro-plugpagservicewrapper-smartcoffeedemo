package br.com.uol.pagseguro.smartcoffee.printer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import br.com.uol.pagseguro.smartcoffee.HomeFragment;
import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.FragmentPrinterBinding;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerPrinterComponent;
import br.com.uol.pagseguro.smartcoffee.injection.PrinterComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;

public class PrinterFragment extends MvpFragment<PrinterContract, PrinterPresenter> implements PrinterContract, HomeFragment {

    PrinterComponent mInjector;

    public static PrinterFragment getInstance() {
        return new PrinterFragment();
    }
    private FragmentPrinterBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInjector = DaggerPrinterComponent
                .builder()
                .mainComponent(((MainActivity) getContext()).getMainComponent())
                .useCaseModule(new UseCaseModule())
                .build();
        binding = FragmentPrinterBinding.inflate(getLayoutInflater());
        mInjector.inject(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickButtons();
    }

    private void clickButtons() {
        binding.btnPrint.setOnClickListener(click -> getPresenter().printFile());
    }

    @Override
    public PrinterPresenter createPresenter() {
        return mInjector.presenter();
    }

    @Override
    public void showSucess() {
        Snackbar.make(getView(), R.string.printer_print_success, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showError(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showFileNotFound() {
        UIFeedback.showDialog(getContext(), R.string.txt_print_test);
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
