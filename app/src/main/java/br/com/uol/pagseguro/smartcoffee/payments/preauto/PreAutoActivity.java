package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_CANCEL_KEYING;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_CARD;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_CONSULT_KEYING;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_EFFETIVATE_KEYING;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED_CREATE;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_1X;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_NUMBER;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_TYPE_A_VISTA;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_TYPE_PARC_VENDEDOR;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TOTAL_VALUE;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TRANSACTION_TYPE;
import static br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_DATA;
import static br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_OPERATION;

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
import br.com.uol.pagseguro.smartcoffee.demo.ActivationDialog;
import br.com.uol.pagseguro.smartcoffee.demo.CustomDialog;
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
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreAutoActivity extends MvpActivity<PreAutoContract, PreAutoPresenter> implements PreAutoContract {
    private static final String TAG = PreAutoActivity.class.getSimpleName();
    private static final int SELECT_INSTALLMENTS_ACTIVITY = 1;
    private static final int PRE_AUTO_KEYED_ACTIVITY = 2;
    private static final int VALUE_MINIMAL_INSTALLMENT = 500;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_auto_options);
        initView();
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
        String installment = data.getStringExtra(INSTALLMENT_NUMBER);
        if (installment == null) {
            UIFeedback.showDialog(this, R.string.text_not_selected_installment);
        } else {
            getPresenter().doPreAutoCreation(
                    mValue,
                    INSTALLMENT_TYPE_PARC_VENDEDOR,
                    Integer.parseInt(installment)
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
                (valueEffectuate, plugPagTransactionResult) ->
                        getPresenter().doPreAutoEffectuate(
                                Integer.parseInt(valueEffectuate),
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
        String installment = data.getStringExtra(INSTALLMENT_NUMBER);
        if (installment == null) {
            UIFeedback.showDialog(this, R.string.text_not_selected_installment);
        } else {
            Intent intent = new Intent(this, PreAutoKeyingActivity.class)
                    .putExtra(PREAUTO_OPERATION, PREAUTO_KEYED_CREATE)
                    .putExtra(TOTAL_VALUE, mValue)
                    .putExtra(INSTALLMENT_NUMBER, Integer.parseInt(installment))
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

    // Start preauto flow
    @OnClick(R.id.btn_create)
    public void btnCreatePreAuto() {
        getPresenter().doPreAutoCreation(mValue, INSTALLMENT_TYPE_A_VISTA, INSTALLMENT_1X);
    }

    // Start preauto digitado a vista flow
    @OnClick(R.id.btn_create_keying)
    public void btnCreatePreAutoKeyedin() {
        startActivitiesPreAutoKeyed(PREAUTO_KEYED_CREATE, INSTALLMENT_TYPE_A_VISTA);
    }

    // Start preauto card installments flow
    @OnClick(R.id.btn_create_installments)
    public void btnCreatePreAutoInstallments() {
        if (mValue < VALUE_MINIMAL_INSTALLMENT) {
            showMessage(getString(R.string.txt_installments_invalid_message));
        } else {
            Intent intent = SelectInstallmentActivity.getStartIntent(
                    getApplicationContext(),
                    mValue,
                    INSTALLMENT_TYPE_PARC_VENDEDOR
            ).putExtra(PREAUTO_OPERATION, PREAUTO_CARD);

            startActivityForResult(intent, SELECT_INSTALLMENTS_ACTIVITY);
        }
    }

    // Start preauto keying flow
    @OnClick(R.id.btn_create_installments_keying)
    public void btnCreatePreAutoInstallmentsKeying() {
        if (mValue < VALUE_MINIMAL_INSTALLMENT) {
            showMessage(getString(R.string.txt_installments_invalid_message));
        } else {
            Intent intent = SelectInstallmentActivity.getStartIntent(
                    getApplicationContext(),
                    mValue,
                    INSTALLMENT_TYPE_PARC_VENDEDOR)
                    .putExtra(TOTAL_VALUE, mValue)
                    .putExtra(PREAUTO_OPERATION, PREAUTO_KEYED);
            startActivityForResult(intent, SELECT_INSTALLMENTS_ACTIVITY);
        }
    }

    @OnClick(R.id.btn_effectuate_cash_keying)
    public void onEffectuatePreAutoClicked() {
        startActivitiesPreAutoKeyed(PREAUTO_EFFETIVATE_KEYING, null);
    }

    @OnClick(R.id.btn_consulta_pre_auto)
    public void onClickConsultaPreAuto() {
        getPresenter().getPreAutoDataEffectivate(
                (valueEffectuate, plugPagTransactionResult) ->
                        getPresenter().doPreAutoEffectuate(
                                Integer.parseInt(valueEffectuate),
                                plugPagTransactionResult.getTransactionId(),
                                plugPagTransactionResult.getTransactionCode()
                        ), null);
    }

    @OnClick(R.id.btn_consulta_pre_auto_dig)
    public void onClickConsultarPreAutoKeydin() {
        startActivitiesPreAutoKeyed(PREAUTO_CONSULT_KEYING, null);
    }

    @OnClick(R.id.btn_cancel_preauto)
    public void onCancelPreAutoClicked() {
        getPresenter().getPreAutoData(true, null);
    }

    @OnClick(R.id.btn_cancel_preauto_keying)
    public void onCancelPreAutoKeydinClicked() {
        startActivitiesPreAutoKeyed(PREAUTO_CANCEL_KEYING, null);
    }

    @OnClick(R.id.btn_report)
    public void onClickReport() {
        getPresenter().getPreAutoData(false, null);
    }

    // End region preauto flow
    // Start region MVP, view implementation
    @Override
    public void showTransactionSuccess() {
        showDialog(getString(R.string.transactions_successful));
    }

    @Override
    public void showError(@Nullable String message) {
        showDialog(message);
    }

    @Override
    public void showMessage(@Nullable String message) {
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
    public void showActivationDialog() {
        ActivationDialog dialog = new ActivationDialog();
        dialog.setOnDismissListener(activationCode -> getPresenter().activate(activationCode));
        dialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void showAuthProgress(@Nullable String message) {
        showDialog(message);
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
    // End region MVP, view implementation

    // MVP, presenter creation
    @NonNull
    @Override
    public PreAutoPresenter createPresenter() {
        return mInjector.presenter();
    }

    // Start region private methods
    private void initDI() {
        mInjector = DaggerPreAutoComponent.builder()
                .useCaseModule(new UseCaseModule())
                .wrapperModule(new WrapperModule(getApplicationContext()))
                .build();
        mInjector.inject(this);
    }

    private void initView() {
        ButterKnife.bind(this);

        dialog = new CustomDialog(this);
        dialog.setOnCancelListener(dialogCancel -> {
            dialogCancel.dismiss();
            getPresenter().abortTransaction();
        });
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
    // End region private methods

    public static Intent getStartIntent(Context context, int totalValue) {
        return new Intent(context, PreAutoActivity.class)
                .putExtra(TOTAL_VALUE, totalValue);
    }
}