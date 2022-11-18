package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_CANCEL_KEYING;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_CARD;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_CONSULT_KEYING;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_EFFETIVATE_KEYING;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED_CREATE;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_1X;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_NUMBER;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TOTAL_VALUE;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TRANSACTION_TYPE;
import static br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_DATA;
import static br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_OPERATION;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_A_VISTA;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_PARC_VENDEDOR;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.MvpActivity;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPreAutoQueryData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.ActivityPreAutoOptionsBinding;
import br.com.uol.pagseguro.smartcoffee.payments.demoInterno.CustomDialog;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerPreAutoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.PreAutoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.injection.WrapperModule;
import br.com.uol.pagseguro.smartcoffee.payments.installments.CustomDialogPreAutoValue;
import br.com.uol.pagseguro.smartcoffee.payments.installments.SelectInstallmentActivity;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.detail.PreAutoDetailActivity;
import br.com.uol.pagseguro.smartcoffee.utils.FileHelper;
import br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;

public class PreAutoActivity extends MvpActivity<PreAutoContract, PreAutoPresenter> implements PreAutoContract {
    private static final String TAG = PreAutoActivity.class.getSimpleName();
    private static final int SELECT_INSTALLMENTS_ACTIVITY = 1;
    private static final int PRE_AUTO_KEYED_ACTIVITY = 2;
    private static final int VALUE_MINIMAL_INSTALLMENT = 1000;

    private CustomDialog dialog;
    private Integer mValue;

    public enum PreAutoOperation {
        PREAUTO_KEYED,
        PREAUTO_CARD,
        PREAUTO_KEYED_CREATE,
        PREAUTO_CONSULT_KEYING,
        PREAUTO_EFFETIVATE_KEYING,
        PREAUTO_CANCEL_KEYING
    }

    @Inject
    PreAutoComponent mInjector;

    private ActivityPreAutoOptionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDI();
        super.onCreate(savedInstanceState);
        binding = ActivityPreAutoOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clickButtons();
        setupExtras();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            PreAutoOperation operation = (PreAutoOperation) data.getExtras().get(PreAutoKeyingConstants.PREAUTO_OPERATION);
            if (operation != null) {
                if (operation.equals(PREAUTO_CARD)) {
                    preAutoCreateInstallmentCard(data);
                } else {
                    checkPreAutoOperation(operation, data);
                }
            } else {
                UIFeedback.showDialog(this, getString(R.string.text_invalid_operation));
            }
        } else {
            UIFeedback.showDialog(this, getString(R.string.text_error_select_installment));
        }
    }

    private void checkPreAutoOperation(PreAutoOperation operation, Intent data) {
        PreAutoPayment preAutoPayment =
                (PreAutoPayment) data.getSerializableExtra(PREAUTO_DATA);

        switch (operation) {
            case PREAUTO_KEYED:
                createPreAutoInstallmentKeyedin(data);
                break;
            case PREAUTO_KEYED_CREATE:
                preAutoCreateFlow(preAutoPayment);
                break;
            case PREAUTO_CONSULT_KEYING:
                preAutoQueryFlow(preAutoPayment);
                break;
            case PREAUTO_EFFETIVATE_KEYING:
                preAutoEffectiveFlow(preAutoPayment);
                break;
            case PREAUTO_CANCEL_KEYING:
                preAutoCancelFlow(preAutoPayment);
                break;
            default:
        }
    }

    private void preAutoCreateInstallmentCard(Intent data) {
        int installment = data.getIntExtra(INSTALLMENT_NUMBER, 0);
        if (installment == 0) {
            UIFeedback.showDialog(this, R.string.text_not_selected_installment);
        } else {
            getPresenter().doPreAutoCreation(
                    mValue,
                    INSTALLMENT_TYPE_PARC_VENDEDOR,
                    installment
            );
        }
    }

    private void preAutoCreateFlow(PreAutoPayment data) {
        String pan = data.getPan();
        String expirationDate = data.getExpirationDate();
        String cardCvv = data.getCardCvv();
        int installment = data.getInstallmentNumber();

        getPresenter().doPreAutoCreation(
                mValue,
                INSTALLMENT_TYPE_PARC_VENDEDOR,
                installment,
                cardCvv,
                expirationDate,
                pan
        );
    }

    private void preAutoQueryFlow(PreAutoPayment data) {
        String pan = data.getPan();
        String expirationDate = data.getExpirationDate();
        String cardCvv = data.getCardCvv();
        String transactionCode = data.getTransactionCode();
        String transactionDate = data.getTransactionDate();

        getPresenter().getPreAutoData(false, new PlugPagPreAutoQueryData(
                mValue,
                INSTALLMENT_TYPE_A_VISTA,
                INSTALLMENT_1X,
                pan,
                cardCvv,
                expirationDate,
                transactionDate,
                transactionCode
        ));
    }

    private void preAutoEffectiveFlow(PreAutoPayment data) {
        String pan = data.getPan();
        String expirationDate = data.getExpirationDate();
        String cardCvv = data.getCardCvv();
        String transactionCode = data.getTransactionCode();
        String transactionDate = data.getTransactionDate();

        getPresenter().getPreAutoDataEffectivate(
                (effectuatedValue, plugPagTransactionResult) ->
                        getPresenter().doPreAutoEffectuate(
                                Integer.parseInt(effectuatedValue),
                                plugPagTransactionResult.getTransactionId(),
                                plugPagTransactionResult.getTransactionCode()
                        ), new PlugPagPreAutoQueryData(
                        mValue,
                        INSTALLMENT_TYPE_PARC_VENDEDOR,
                        INSTALLMENT_1X,
                        pan,
                        cardCvv,
                        expirationDate,
                        transactionDate,
                        transactionCode
                ));
    }

    private void preAutoCancelFlow(PreAutoPayment data) {
        String pan = data.getPan();
        String expirationDate = data.getExpirationDate();
        String cardCvv = data.getCardCvv();
        String transactionCode = data.getTransactionCode();
        String transactionDate = data.getTransactionDate();

        getPresenter().getPreAutoData(true, new PlugPagPreAutoQueryData(
                mValue,
                INSTALLMENT_TYPE_PARC_VENDEDOR,
                INSTALLMENT_1X,
                pan,
                cardCvv,
                expirationDate,
                transactionDate,
                transactionCode
        ));
    }

    public void createPreAutoInstallmentKeyedin(Intent data) {
        int installment = data.getIntExtra(INSTALLMENT_NUMBER, 0);
        if (installment == 0) {
            UIFeedback.showDialog(this, R.string.text_not_selected_installment);
        } else {
            Intent intent = new Intent(this, PreAutoKeyingActivity.class)
                    .putExtra(PREAUTO_OPERATION, PREAUTO_KEYED_CREATE)
                    .putExtra(TOTAL_VALUE, mValue)
                    .putExtra(INSTALLMENT_NUMBER, installment)
                    .putExtra(TRANSACTION_TYPE, INSTALLMENT_TYPE_PARC_VENDEDOR);

            startActivityForResult(intent, PRE_AUTO_KEYED_ACTIVITY);
        }
    }

    private void startActivitiesPreAutoKeyed(PreAutoOperation operation,
                                             @Nullable Integer transactionType) {
        Intent intent = new Intent(this, PreAutoKeyingActivity.class)
                .putExtra(TRANSACTION_TYPE, transactionType)
                .putExtra(TOTAL_VALUE, mValue)
                .putExtra(INSTALLMENT_NUMBER, INSTALLMENT_1X)
                .putExtra(PREAUTO_OPERATION, operation);

        startActivityForResult(intent, PRE_AUTO_KEYED_ACTIVITY);
    }

    @Override
    public void showTransactionSuccess() {
        showDialog(getString(R.string.transactions_successful));
    }

    @Override
    public void showTransactionDialog(@Nullable String message) {
        showDialog(message);
    }

    @Override
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
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
    public void writeToFile(@Nullable String transactionCode, @Nullable String transactionId) {
        FileHelper.writeToFile(transactionCode, transactionId, this);
    }

    @Override
    public void showDialogValuePreAuto(
            DismissListenerEffectivate onDismissListener,
            PlugPagTransactionResult plugPagTransactionResult
    ) {
        CustomDialogPreAutoValue dialog = new CustomDialogPreAutoValue();
        int amountPreAuto = 0;

        if (plugPagTransactionResult.getAmount() != null) {
            amountPreAuto = Integer.parseInt(plugPagTransactionResult.getAmount());
        }

        if (amountPreAuto > 0) {
            dialog.setValueTotalCreatePreAuto(amountPreAuto);
        }

        dialog.setOnDismissListener(onDismissListener);
        dialog.setPlugPagTransactionResult(plugPagTransactionResult);
        dialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void showPreAutoDetail(
            PlugPagTransactionResult plugPagTransactionResult,
            Boolean isPreAutoCancel
    ) {
        startActivity(
                PreAutoDetailActivity.getPreAutoDetailActivityIntent(
                        this,
                        plugPagTransactionResult,
                        isPreAutoCancel
                )
        );
    }

    @NonNull
    @Override
    public PreAutoPresenter createPresenter() {
        return mInjector.presenter();
    }

    private void initDI() {
        mInjector = DaggerPreAutoComponent.builder()
                .useCaseModule(new UseCaseModule())
                .wrapperModule(new WrapperModule(getApplicationContext()))
                .build();
        mInjector.inject(this);
    }

    private void clickButtons() {
        dialog = new CustomDialog(this);
        dialog.setOnCancelListener(dialogCancel -> {
            dialogCancel.dismiss();
            getPresenter().abortTransaction();
        });
        binding.btnCreate.setOnClickListener(click ->
                getPresenter().doPreAutoCreation(mValue, INSTALLMENT_TYPE_A_VISTA, INSTALLMENT_1X)
        );
        binding.btnCreateKeying.setOnClickListener(click ->
                startActivitiesPreAutoKeyed(PREAUTO_KEYED_CREATE, INSTALLMENT_TYPE_A_VISTA)
        );
        binding.btnCreateInstallments.setOnClickListener(click -> {
            if (mValue < VALUE_MINIMAL_INSTALLMENT) {
                showTransactionDialog(getString(R.string.txt_installments_invalid_message));
            } else {
                Intent intent = SelectInstallmentActivity.getStartIntent(
                        getApplicationContext(),
                        mValue,
                        INSTALLMENT_TYPE_PARC_VENDEDOR
                ).putExtra(PREAUTO_OPERATION, PREAUTO_CARD);

                startActivityForResult(intent, SELECT_INSTALLMENTS_ACTIVITY);
            }
        });
        binding.btnEffectuateCash.setOnClickListener(click ->
                getPresenter().getPreAutoDataEffectivate(
                        (effectuatedValue, plugPagTransactionResult) ->
                                getPresenter().doPreAutoEffectuate(
                                        Integer.parseInt(effectuatedValue),
                                        plugPagTransactionResult.getTransactionId(),
                                        plugPagTransactionResult.getTransactionCode()
                                ),
                        null
                )
        );
        binding.btnCreateInstallmentsKeying.setOnClickListener(click -> {
            if (mValue < VALUE_MINIMAL_INSTALLMENT) {
                showTransactionDialog(getString(R.string.txt_installments_invalid_message));
            } else {
                Intent intent = SelectInstallmentActivity.getStartIntent(
                                getApplicationContext(),
                                mValue,
                                INSTALLMENT_TYPE_PARC_VENDEDOR)
                        .putExtra(TOTAL_VALUE, mValue)
                        .putExtra(PREAUTO_OPERATION, PREAUTO_KEYED);
                startActivityForResult(intent, SELECT_INSTALLMENTS_ACTIVITY);
            }
        });
        binding.btnEffectuateCashKeying.setOnClickListener(click ->
                startActivitiesPreAutoKeyed(PREAUTO_EFFETIVATE_KEYING, null)
        );
        binding.btnConsultaPreAuto.setOnClickListener(click ->
                getPresenter().getPreAutoData(false, null)
        );
        binding.btnConsultaPreAutoDig.setOnClickListener(click ->
                startActivitiesPreAutoKeyed(PREAUTO_CONSULT_KEYING, null)
        );
        binding.btnCancelPreauto.setOnClickListener(click ->
                getPresenter().getPreAutoData(true, null)
        );
        binding.btnCancelPreautoKeying.setOnClickListener(click ->
                startActivitiesPreAutoKeyed(PREAUTO_CANCEL_KEYING, null)
        );
        binding.btnReport.setOnClickListener(click ->
                getPresenter().getPreAutoData(false, null)
        );
    }

    private void setupExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mValue = extras.getInt(TOTAL_VALUE);
        }
    }

    private void showDialog(String message) {
        if (!dialog.isShowing()) {
            dialog.show();
        }
        dialog.setMessage(message);
    }

    public static Intent getStartIntent(Context context, int totalValue) {
        return new Intent(context, PreAutoActivity.class)
                .putExtra(TOTAL_VALUE, totalValue);
    }
}