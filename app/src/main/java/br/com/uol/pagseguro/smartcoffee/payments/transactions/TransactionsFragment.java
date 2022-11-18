package br.com.uol.pagseguro.smartcoffee.payments.transactions;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.ActionResult;
import br.com.uol.pagseguro.smartcoffee.HomeFragment;
import br.com.uol.pagseguro.smartcoffee.MainActivity;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.FragmentTransactionsBinding;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerTransactionsComponent;
import br.com.uol.pagseguro.smartcoffee.injection.TransactionsComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity;
import br.com.uol.pagseguro.smartcoffee.utils.FileHelper;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;

public class TransactionsFragment extends MvpFragment<TransactionsContract, TransactionsPresenter> implements TransactionsContract, HomeFragment {

    @Inject
    TransactionsComponent mInjector;

    public static TransactionsFragment getInstance() {
        return new TransactionsFragment();
    }

    private FragmentTransactionsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInjector = DaggerTransactionsComponent.builder()
                .useCaseModule(new UseCaseModule())
                .mainComponent(((MainActivity) getContext()).getMainComponent())
                .build();
        binding = FragmentTransactionsBinding.inflate(getLayoutInflater());
        mInjector.inject(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickButtons();
    }

    private void clickButtons() {
        binding.btnSmartposPreauto.setOnClickListener(click -> {
            new PreAutoActivity();
            Intent intent = PreAutoActivity.getStartIntent(getContext(), getPresenter().getAmount());
            startActivity(intent);
        });
        binding.btnSmartposCredit.setOnClickListener(click ->
                getPresenter().creditPayment()
        );
        binding.btnSmartposCreditWithSellerInstallments.setOnClickListener(click ->
                getPresenter().doCreditPaymentWithSellerInstallments()
        );
        binding.btnSmartposCreditWithBuyerInstallments.setOnClickListener(click ->
                getPresenter().doCreditPaymentWithBuyerInstallments()
        );
        binding.btnSmartposDebit.setOnClickListener(click ->
                getPresenter().doDebitPayment()
        );
        binding.btnSmartposVoucher.setOnClickListener(click ->
                getPresenter().doVoucherPayment()
        );
        binding.btnSmartposVoidPayment.setOnClickListener(click -> {
            ActionResult actionResult = FileHelper.readFromFile(getContext());
            getPresenter().doRefundPayment(actionResult);
        });
        binding.btnSmartposVoidPrintStablishment.setOnClickListener(click ->
                getPresenter().printStablishmentReceipt()
        );
        binding.btnSmartposVoidPrintCustomer.setOnClickListener(click ->
                getPresenter().printCustomerReceipt()
        );
        binding.btnSmartposGetLastTransaction.setOnClickListener(click ->
                getPresenter().getLastTransaction()
        );
        binding.btnSmartposDebitCarne.setOnClickListener(click ->
                getPresenter().doDebitCarnePayment()
        );
        binding.btnSmartposCreditCarne.setOnClickListener(click ->
                getPresenter().doCreditCarnePayment()
        );
        binding.btnSmartposGetCardData.setOnClickListener(click ->
                getPresenter().getCardData()
        );
    }

    @Override
    public TransactionsPresenter createPresenter() {
        return mInjector.presenter();
    }

    @Override
    public void showTransactionSuccess() {
        UIFeedback.showDialog(getContext(), R.string.transactions_successful);
    }

    @Override
    public void writeToFile(String transactionCode, String transactionId) {
        FileHelper.writeToFile(transactionCode, transactionId, getContext());
    }

    @Override
    public void showMessage(String message) {
        UIFeedback.showDialog(getContext(), message, cancelListener);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            UIFeedback.showProgress(getContext());
        } else {
            UIFeedback.dismissProgress();
        }
    }

    DialogInterface.OnCancelListener cancelListener = dialogInterface -> {
        dialogInterface.dismiss();
        getPresenter().abortTransaction();
    };

    public void showLastTransaction(String transactionCode) {
        UIFeedback.showDialog(getContext(), transactionCode);
    }

    @Override
    public void onDestroy() {
        UIFeedback.releaseVariables();
        super.onDestroy();
    }
}
