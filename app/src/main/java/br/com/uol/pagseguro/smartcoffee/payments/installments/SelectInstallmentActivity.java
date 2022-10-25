package br.com.uol.pagseguro.smartcoffee.payments.installments;

import static br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_OPERATION;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_PARC_VENDEDOR;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import javax.inject.Inject;

import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.ActivitySelectInstallmentBinding;
import br.com.uol.pagseguro.smartcoffee.demoInterno.ActivationDialog;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerSelectInstallmentComponent;
import br.com.uol.pagseguro.smartcoffee.injection.SelectInstallmentComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.injection.WrapperModule;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity;
import br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants;
import br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;
import butterknife.ButterKnife;

public class SelectInstallmentActivity extends MvpActivity<SelectInstallmentContract, SelectInstallmentPresenter>
        implements SelectInstallmentContract {

    private static final String TAG = SelectInstallmentActivity.class.getSimpleName();
    private PreAutoActivity.PreAutoOperation preAutoOperation;
    private Integer mValue;
    private Integer mParcType;
    private Boolean isPreAutoKeyed = false;
    private ActivitySelectInstallmentBinding binding;

    @Inject
    SelectInstallmentComponent mInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDI();
        super.onCreate(savedInstanceState);

        binding = ActivitySelectInstallmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ButterKnife.bind(this);
        this.setTitle(R.string.text_select_installment);
        initExtras();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().calculateInstallments(mValue, mParcType, isPreAutoKeyed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIFeedback.releaseVariables();
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
    public void setUpAdapter(List<String> installments) {
        final InstallmentsAdapter adapter = new InstallmentsAdapter(installments, itemClick -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(InstallmentConstants.TOTAL_VALUE, mValue);
            returnIntent.putExtra(InstallmentConstants.INSTALLMENT_NUMBER, itemClick);
            returnIntent.putExtra(InstallmentConstants.TRANSACTION_TYPE, mParcType);
            returnIntent.putExtra(PREAUTO_OPERATION, preAutoOperation);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
        binding.listInstallments.setAdapter(adapter);
    }

    @Override
    public void showError(@Nullable String message) {
        UIFeedback.showDialog(this, message);
    }

    @Override
    public void showActivationDialog() {
        ActivationDialog dialog = new ActivationDialog();
        dialog.setOnDismissListener(activationCode -> getPresenter().activate(activationCode));
        dialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void showAuthProgress(@Nullable String message) {
        UIFeedback.showDialog(this, message);
    }

    @NonNull
    @Override
    public SelectInstallmentPresenter createPresenter() {
        return mInjector.presenter();
    }

    private void initExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mValue = extras.getInt(InstallmentConstants.TOTAL_VALUE);
            preAutoOperation = (PreAutoActivity.PreAutoOperation)extras.get(PreAutoKeyingConstants.PREAUTO_OPERATION);
            if (preAutoOperation != null && preAutoOperation == PreAutoActivity.PreAutoOperation.PREAUTO_KEYED) {
                isPreAutoKeyed = true;
            }
            if (getIntent().hasExtra(InstallmentConstants.TRANSACTION_TYPE)) {
                mParcType = extras.getInt(InstallmentConstants.TRANSACTION_TYPE);
            } else {
                mParcType = INSTALLMENT_TYPE_PARC_VENDEDOR;
            }
        }
    }

    private void initDI() {
        mInjector = DaggerSelectInstallmentComponent.builder()
                .useCaseModule(new UseCaseModule())
                .wrapperModule(new WrapperModule(getApplicationContext()))
                .build();
        mInjector.inject(this);
    }
    // End region private methods

    public static Intent getStartIntent(
            Context context,
            int totalValue,
            int transactionType
    ) {
        Intent intent = new Intent(context, SelectInstallmentActivity.class);
        intent.putExtra(InstallmentConstants.TOTAL_VALUE, totalValue);
        intent.putExtra(InstallmentConstants.TRANSACTION_TYPE, transactionType);
        return intent;
    }
}