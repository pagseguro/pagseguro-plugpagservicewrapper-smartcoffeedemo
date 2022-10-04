package br.com.uol.pagseguro.smartcoffee.payments.preauto.detail;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hannesdorfmann.mosby.mvp.MvpActivity;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.demo.CustomDialog;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerPreAutoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.PreAutoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.injection.WrapperModule;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreAutoDetailActivity extends MvpActivity<PreAutoDetailContract, PreAutoDetailPresenter>
    implements PreAutoDetailContract {

    @BindView(R.id.container_detail_success) RelativeLayout containerSuccess;
    @BindView(R.id.container_detail_error) LinearLayout containerError;
    @BindView(R.id.btn_preauto_cancel) Button btnCancel;

    @BindView(R.id.txt_transaction_auto) TextView txtAUTO;
    @BindView(R.id.txt_transaction_amount) TextView txtAmount;
    @BindView(R.id.txt_transaction_card_brand) TextView txtCardBrand;
    @BindView(R.id.txt_transaction_holder_name) TextView txtHolderName;
    @BindView(R.id.txt_transaction_holder) TextView txtHolder;
    @BindView(R.id.txt_transaction_date) TextView txtDate;
    @BindView(R.id.txt_transaction_nsu) TextView txtNSU;
    @BindView(R.id.txt_transaction_payment_type) TextView txtPaymentType;
    @BindView(R.id.txt_transaction_type) TextView txtTransactionType;
    @BindView(R.id.txt_installments_val) TextView txtInstallmentsValue;

    @BindView(R.id.txt_error_code) TextView txtErrorCode;
    @BindView(R.id.txt_error_message) TextView txtErrorMessage;

    private static final String PREAUTO_DETAIL = "PREAUTO_DETAIL";
    private static Boolean isPreAutoCancel = false;

    private String transactionId = "";
    private String transactionCode = "";
    private CustomDialog dialog;

    @Inject PreAutoComponent mInjector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initDI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_auto_detail);
        ButterKnife.bind(this);
        initListener();
        bindUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIFeedback.releaseVariables();
    }

    @NonNull
    @Override
    public PreAutoDetailPresenter createPresenter() {
        return mInjector.preAutoDetailPresenter();
    }

    @OnClick(R.id.btn_preauto_cancel)
    public void cancelPreAuto() {
        if (!transactionId.isEmpty() && !transactionCode.isEmpty() && isPreAutoCancel) {
            getPresenter().doPreAutoCancel(transactionId, transactionCode);
        }
    }

    @Override
    public void showTransactionSuccess() {
        showDialog(getString(R.string.preauto_cancel_successful));
    }

    @Override
    public void showMessage(String message) {
        showDialog(message);
    }

    @Override
    public void showError(String error) {
        showDialog(error);
    }

    @Override
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static Intent getPreAutoDetailActivityIntent(
        Context context,
        PlugPagTransactionResult plugPagTransactionResult,
        Boolean isPreAutoCancel
    ) {
        PreAutoDetailActivity.isPreAutoCancel = isPreAutoCancel;
        return new Intent(context, PreAutoDetailActivity.class)
            .putExtra(PREAUTO_DETAIL, plugPagTransactionResult);
    }

    private void bindUI() {
        PlugPagTransactionResult plugPagTransactionResult = getIntent().getParcelableExtra(PREAUTO_DETAIL);

        if (plugPagTransactionResult != null) {
            if (plugPagTransactionResult.getResult() != null &&
                    plugPagTransactionResult.getResult() == 0) {

                showLabelsSuccess();

                txtAUTO.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getAutoCode()));
                txtCardBrand.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getCardBrand()));
                txtHolderName.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getHolderName()));
                txtHolder.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getHolder()));
                txtDate.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getDate()));
                txtNSU.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getNsu()));
                txtTransactionType.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getTypeTransaction()));

                if(plugPagTransactionResult.getInstallments() != null) {
                    txtInstallmentsValue.setText(String.valueOf((int) plugPagTransactionResult.getInstallments()));
                }

                if (!plugPagTransactionResult.getAmount().isEmpty()) {
                    txtAmount.setText(Utils.getFormattedValue(Double.valueOf(plugPagTransactionResult.getAmount())));
                } else {
                    txtAmount.setText(getString(R.string.txt_not_value));
                }

                if (plugPagTransactionResult.getPaymentType() <= 0) {
                    txtPaymentType.setText(Utils.VALUE_NULL_OR_EMPTY);
                } else {
                    txtPaymentType.setText(plugPagTransactionResult.getPaymentType().toString());
                }

                transactionId = Utils.isNotNullOrEmpty(plugPagTransactionResult.getTransactionId());
                transactionCode = Utils.isNotNullOrEmpty(plugPagTransactionResult.getTransactionCode());
            } else {
                showLabelErrors();

                txtErrorCode.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getErrorCode()));
                txtErrorMessage.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getMessage()));
            }
        } else {
            showLabelErrors();

            txtErrorCode.setText(getString(R.string.txt_error_code));
            txtErrorMessage.setText(getString(R.string.txt_msg_default_error));
        }
    }

    private void showLabelErrors() {
        containerError.setVisibility(View.VISIBLE);
        containerSuccess.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }

    private void showLabelsSuccess() {
        containerSuccess.setVisibility(View.VISIBLE);
        containerError.setVisibility(View.GONE);

        if (isPreAutoCancel) {
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnCancel.setVisibility(View.GONE);
        }
    }

    private void initDI() {
        mInjector = DaggerPreAutoComponent.builder()
                .useCaseModule(new UseCaseModule())
                .wrapperModule(new WrapperModule(getApplicationContext()))
                .build();
        mInjector.inject(this);
    }

    private void initListener() {
        dialog = new CustomDialog(this);
        dialog.setOnCancelListener(DialogInterface::dismiss);
    }

    private void showDialog(String message) {
        if (!dialog.isShowing()) {
            dialog.show();
        }

        dialog.setMessage(message);
    }

    @Override
    public void closeActivity() {
        Handler handler = new Handler();
        handler.postDelayed(this::finish, 2000);
    }
}