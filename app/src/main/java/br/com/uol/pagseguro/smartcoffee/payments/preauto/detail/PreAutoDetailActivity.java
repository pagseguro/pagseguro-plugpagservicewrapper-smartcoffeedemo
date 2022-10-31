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
import br.com.uol.pagseguro.smartcoffee.databinding.ActivityPreAutoDetailBinding;
import br.com.uol.pagseguro.smartcoffee.demoInterno.CustomDialog;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerPreAutoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.PreAutoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.injection.WrapperModule;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;

public class PreAutoDetailActivity extends MvpActivity<PreAutoDetailContract, PreAutoDetailPresenter>
    implements PreAutoDetailContract {

    private static final String PREAUTO_DETAIL = "PREAUTO_DETAIL";
    private static Boolean isPreAutoCancel = false;
    private ActivityPreAutoDetailBinding binding;

    private String transactionId = "";
    private String transactionCode = "";
    private CustomDialog dialog;

    @Inject PreAutoComponent mInjector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initDI();
        super.onCreate(savedInstanceState);
        binding = ActivityPreAutoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

                binding.txtTransactionAuto.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getAutoCode()));
                binding.txtTransactionCardBrand.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getCardBrand()));
                binding.txtTransactionHolderName.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getHolderName()));
                binding.txtTransactionHolder.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getHolder()));
                binding.txtTransactionDate.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getDate()));
                binding.txtTransactionNsu.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getNsu()));
                binding.txtTransactionType.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getTypeTransaction()));

                if(plugPagTransactionResult.getInstallments() != null) {
                    binding.txtInstallmentsVal.setText(String.valueOf((int) plugPagTransactionResult.getInstallments()));
                }

                if (!plugPagTransactionResult.getAmount().isEmpty()) {
                    binding.txtTransactionAmount.setText(Utils.getFormattedValue(Double.valueOf(plugPagTransactionResult.getAmount())));
                } else {
                    binding.txtTransactionAmount.setText(getString(R.string.txt_not_value));
                }

                if (plugPagTransactionResult.getPaymentType() <= 0) {
                    binding.txtTransactionPaymentType.setText(Utils.VALUE_NULL_OR_EMPTY);
                } else {
                    binding.txtTransactionPaymentType.setText(plugPagTransactionResult.getPaymentType().toString());
                }

                transactionId = Utils.isNotNullOrEmpty(plugPagTransactionResult.getTransactionId());
                transactionCode = Utils.isNotNullOrEmpty(plugPagTransactionResult.getTransactionCode());
            } else {
                showLabelErrors();

                binding.txtErrorCode.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getErrorCode()));
                binding.txtErrorMessage.setText(Utils.isNotNullOrEmpty(plugPagTransactionResult.getMessage()));
            }
        } else {
            showLabelErrors();

            binding.txtErrorCode.setText(getString(R.string.txt_error_code));
            binding.txtErrorMessage.setText(getString(R.string.txt_msg_default_error));
        }
    }

    private void showLabelErrors() {
        binding.containerDetailError.setVisibility(View.VISIBLE);
        binding.containerDetailSuccess.setVisibility(View.GONE);
        binding.btnPreautoCancel.setVisibility(View.GONE);
    }

    private void showLabelsSuccess() {
        binding.containerDetailSuccess.setVisibility(View.VISIBLE);
        binding.containerDetailError.setVisibility(View.GONE);

        if (isPreAutoCancel) {
            binding.btnPreautoCancel.setVisibility(View.VISIBLE);
        } else {
            binding.btnPreautoCancel.setVisibility(View.GONE);
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
        binding.btnPreautoCancel.setOnClickListener(click -> {
            if (!transactionId.isEmpty() && !transactionCode.isEmpty() && isPreAutoCancel) {
                getPresenter().doPreAutoCancel(transactionId, transactionCode);
            }
        });
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