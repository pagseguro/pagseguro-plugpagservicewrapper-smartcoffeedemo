package br.com.uol.pagseguro.smartcoffee.demoInterno;

import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CREDIT_VALUE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import java.text.NumberFormat;
import java.util.Locale;
import javax.inject.Inject;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.payments.credit.CreditPaymentActivity;
import br.com.uol.pagseguro.smartcoffee.injection.DaggerDemoInternoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.DemoInternoComponent;
import br.com.uol.pagseguro.smartcoffee.injection.UseCaseModule;
import br.com.uol.pagseguro.smartcoffee.injection.WrapperModule;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity;
import br.com.uol.pagseguro.smartcoffee.payments.qrcode.QrcodeActivity;
import br.com.uol.pagseguro.smartcoffee.utils.FileHelper;
import br.com.uol.pagseguro.smartcoffee.utils.UIFeedback;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DemoInternoActivity extends MvpActivity<DemoInternoContract, DemoInternoPresenter> implements DemoInternoContract {

    CustomDialog dialog;

    @Inject
    DemoInternoComponent mInjector;

    @BindView(R.id.txtTotalValue)
    EditText mTotalValue;
    private boolean shouldShowDialog;

    private boolean mCanClick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mInjector = DaggerDemoInternoComponent.builder()
                .useCaseModule(new UseCaseModule())
                .wrapperModule(new WrapperModule(getApplicationContext()))
                .build();
        mInjector.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_selection);
        ButterKnife.bind(this);
        initPropertiesAndListeners();
    }

    private void initPropertiesAndListeners() {
        dialog = new CustomDialog(this);
        dialog.setOnCancelListener(cancelListener);
        mTotalValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //DoNothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String typed = onlyDigits(charSequence.toString());
                if (!TextUtils.isEmpty(typed)) {
                    mTotalValue.removeTextChangedListener(this);
                    double converted = Double.parseDouble(typed) / 100;
                    String convertedString = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(converted);
                    mTotalValue.setText(convertedString);
                    mTotalValue.setSelection(convertedString.length());
                    mTotalValue.addTextChangedListener(this);
                } else {
                    mTotalValue.setText("0");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //DoNothing
            }
        });
    }


    @NonNull
    @Override
    public DemoInternoPresenter createPresenter() {
        return mInjector.presenter();
    }

    private static String onlyDigits(String textValue) {
        return textValue.replaceAll("[^\\d]", "");
    }

    private int getValue() {
        return Integer.parseInt(mTotalValue.getText().toString().replaceAll("[^0-9]*", ""));
    }

    @OnClick(R.id.txtTotalValue)
    public void onAmmountClicked() {
        openKeyboard();
    }

    private void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTotalValue, InputMethodManager.SHOW_FORCED);
    }

    @OnClick(R.id.btnDebit)
    public void onDebitClicked() {
        if (!mCanClick) {
            return;
        }
        mCanClick = false;
        shouldShowDialog = true;
        getPresenter().doDebitPayment(getValue());
    }

    @OnClick(R.id.btnDebitCarne)
    public void onDebitCarneClicked() {
        if (!mCanClick) {
            return;
        }
        mCanClick = false;
        shouldShowDialog = true;
        getPresenter().doDebitCarnePayment(getValue());
    }

    @OnClick(R.id.btnCredit)
    public void onCreditClicked() {
        Intent intent = new Intent(this, CreditPaymentActivity.class);
        intent.putExtra(CREDIT_VALUE, getValue());
        startActivity(intent);
    }

    @OnClick(R.id.btnVoucher)
    public void onVoucherClicked() {
        if (!mCanClick) {
            return;
        }
        mCanClick = false;
        shouldShowDialog = true;
        getPresenter().doVoucherPayment(getValue());
    }

    @OnClick(R.id.btnQRCode)
    public void onQRCodeClicked() {
        Intent intent = new Intent(this, QrcodeActivity.class);
        intent.putExtra(QrcodeActivity.TAG, getValue());
        startActivity(intent);
    }

    @OnClick(R.id.btnPix)
    public void onPixClicked() {
        if (!mCanClick) {
            return;
        }
        mCanClick = false;
        shouldShowDialog = true;
        getPresenter().pixPayment(getValue());
    }

    @OnClick(R.id.btn_pre_auto)
    public void onClickPreAuto() {
        Intent intent = new PreAutoActivity().getStartIntent(getApplicationContext(), getValue());
        startActivity(intent);
    }

    @OnClick(R.id.btnLastTransaction)
    public void lastTransaction() {
        shouldShowDialog = true;
        getPresenter().getLastTransaction();
    }

    @OnClick(R.id.btnRefund)
    public void onRefundClicked() {
        if (!mCanClick) {
            return;
        }

        shouldShowDialog = true;
        mCanClick = false;
        getPresenter().doRefund(FileHelper.readFromFile(this));
    }

    @Override
    public void showTransactionSuccess() {
        mCanClick = true;
        showMessage(getString(R.string.transactions_successful));
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
    public void showAuthProgress(String message) {
        UIFeedback.showDialog(this, message);
    }

    @Override
    public void setPaymentValue(String value) {
        mTotalValue.setText(value);
    }

    @Override
    public void showMessage(String message) {
        if (shouldShowDialog && !dialog.isShowing()) {
            dialog.show();
        }
        dialog.setMessage(message);
    }

    @Override
    public void showError(String message) {
        UIFeedback.showDialog(this, message);
    }

    @Override
    public void showAbortedSuccessfully() {
        UIFeedback.showDialog(this, R.string.transactions_successful_abort, true);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            UIFeedback.showProgress(this);
        } else {
            UIFeedback.dismissProgress();
        }
    }

    @Override
    public void showActivationDialog() {
        ActivationDialog dialog = new ActivationDialog();
        dialog.setOnDismissListener(activationCode -> getPresenter().activate(activationCode));
        dialog.show(getSupportFragmentManager(), CreditPaymentActivity.ACTIVATION_DIALOG);
    }

    DialogInterface.OnCancelListener cancelListener = dialogInterface -> {
        dialogInterface.dismiss();
        if (shouldShowDialog) {
            getPresenter().abortTransaction();
        }
    };

    @Override
    public void onDestroy() {
        UIFeedback.releaseVariables();
        super.onDestroy();
    }
}
