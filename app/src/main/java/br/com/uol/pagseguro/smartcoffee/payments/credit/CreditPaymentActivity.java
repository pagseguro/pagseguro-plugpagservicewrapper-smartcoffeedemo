package br.com.uol.pagseguro.smartcoffee.payments.credit;

import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_NUMBER;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TOTAL_VALUE;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TRANSACTION_TYPE;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CREDIT_VALUE;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_PARC_COMPRADOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_PARC_VENDEDOR;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.VALUE_MINIMAL_INSTALLMENT;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.MvpActivity;

import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.ActivityCreditPaymentBinding;
import br.com.uol.pagseguro.smartcoffee.demoInterno.ActivationDialog;
import br.com.uol.pagseguro.smartcoffee.demoInterno.CustomDialog;
import br.com.uol.pagseguro.smartcoffee.injection.CreditComponent;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerCreditComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.injection.WrapperModule;
import br.com.uol.pagseguro.smartcoffee.payments.installments.SelectInstallmentActivity;
import br.com.uol.pagseguro.smartcoffee.utils.FileHelper;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;
import butterknife.ButterKnife;

public class CreditPaymentActivity extends MvpActivity<CreditPaymentContract, CreditPaymentPresenter>
        implements CreditPaymentContract {

    private int value;
    CustomDialog dialog;
    private ActivityCreditPaymentBinding binding;

    private static final int LAUNCH_INSTALLMENTS_ACTIVITY = 1;
    public static final String ACTIVATION_DIALOG = "dialog";

    private boolean shouldShowDialog;
    private boolean mCanClick = true;

    @Inject
    CreditComponent mInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        daggerInitializer();
        binding = ActivityCreditPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);

        getCreditValue(getIntent().getExtras());
        viewsInitializer();
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public CreditPaymentPresenter createPresenter() {
        return mInjector.presenter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startPayment(requestCode, resultCode, data);
    }

    private void startPayment(int requestCode, int resultCode, Intent data) {
        if (requestCode != LAUNCH_INSTALLMENTS_ACTIVITY) {
            UIFeedback.showDialog(this, R.string.text_error_select_installment);
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            final int amount = data.getIntExtra(TOTAL_VALUE, 0);
            final int transactionType = data.getIntExtra(TRANSACTION_TYPE, 0);
            final String installmentNumber = data.getStringExtra(INSTALLMENT_NUMBER);

            if (installmentNumber == null) {
                return;
            }
            switch (transactionType) {
                case INSTALLMENT_TYPE_PARC_COMPRADOR:
                    getPresenter().creditPaymentBuyerInstallments(amount, Integer.parseInt(installmentNumber));
                    break;
                case INSTALLMENT_TYPE_PARC_VENDEDOR:
                    getPresenter().creditPaymentSellerInstallments(amount, Integer.parseInt(installmentNumber));
                    break;
                default:
                    break;
            }
        } else {
            mCanClick = true;
            UIFeedback.showDialog(this, R.string.text_not_selected_installment);
        }
    }

    private void daggerInitializer() {
        mInjector = DaggerCreditComponent.builder()
                .useCaseModule(new UseCaseModule())
                .wrapperModule(new WrapperModule(getApplicationContext()))
                .build();
        mInjector.inject(this);
    }

    private void viewsInitializer() {
        dialog = new CustomDialog(this);
        dialog.setOnCancelListener(cancelListener);

        binding.btnCredit.setOnClickListener(click -> {
            if (!mCanClick) {
                return;
            }
            mCanClick = false;
            shouldShowDialog = true;
            getPresenter().creditPaymentInCash(value);
        });
        binding.btnCreditBuyer.setOnClickListener(click ->
                startInstallmentActivity(INSTALLMENT_TYPE_PARC_COMPRADOR)
        );
        binding.btnCreditSeller.setOnClickListener(click ->
                startInstallmentActivity(INSTALLMENT_TYPE_PARC_VENDEDOR)
        );
        binding.btnCreditCarne.setOnClickListener(click -> {
            if (!mCanClick) {
                return;
            }
            mCanClick = false;
            shouldShowDialog = true;
            getPresenter().creditCarnePayment(value);
        });
    }

    private void getCreditValue(Bundle extras) {
        if (extras != null) {
            value = extras.getInt(CREDIT_VALUE);
        } else {
            value = 0;
        }
    }

    DialogInterface.OnCancelListener cancelListener = dialogInterface -> {
        dialogInterface.dismiss();
        if (shouldShowDialog) {
            getPresenter().abortTransaction();
        }
    };

    private void showDialog(String message) {
        if (!dialog.isShowing()) {
            dialog.show();
        }

        dialog.setMessage(message);
    }

    @Override
    public void showTransactionSuccess() {
        mCanClick = true;
        showMessage(getString(R.string.transactions_successful));
    }

    @Override
    public void showError(String message) {
        showDialog(message);
    }

    @Override
    public void showMessage(String message) {
        if (shouldShowDialog && !dialog.isShowing()) {
            dialog.show();
        }
        dialog.setMessage(message);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            UIFeedback.showProgress(this);
        } else {
            UIFeedback.dismissProgress();
            UIFeedback.releaseVariables();
        }
    }

    @Override
    public void writeToFile(String transactionCode, String transactionId) {
        FileHelper.writeToFile(transactionCode, transactionId, this);
    }

    @Override
    public void disposeDialog() {
        mCanClick = true;
        shouldShowDialog = false;
    }

    @Override
    public void showActivationDialog() {
        ActivationDialog dialog = new ActivationDialog();
        dialog.setOnDismissListener(activationCode -> getPresenter().activate(activationCode));
        dialog.show(getSupportFragmentManager(), ACTIVATION_DIALOG);
    }

    @Override
    public void showAuthProgress(String message) {
        showDialog(message);
    }

    private void startInstallmentActivity(int creditType) {
        if (!mCanClick) {
            return;
        }
        mCanClick = false;
        shouldShowDialog = true;
        if (value < VALUE_MINIMAL_INSTALLMENT) {
            showMessage(getString(R.string.txt_installments_invalid_message));
            return;
        }
        Intent intent = SelectInstallmentActivity.getStartIntent(
                getApplicationContext(),
                value,
                creditType
        );
        startActivityForResult(intent, LAUNCH_INSTALLMENTS_ACTIVITY);
    }
}
