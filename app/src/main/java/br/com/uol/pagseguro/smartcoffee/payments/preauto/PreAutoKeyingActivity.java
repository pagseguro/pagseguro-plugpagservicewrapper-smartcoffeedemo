package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED;
import static br.com.uol.pagseguro.smartcoffee.payments.preauto.PreAutoActivity.PreAutoOperation.PREAUTO_KEYED_CREATE;
import static br.com.uol.pagseguro.smartcoffee.payments.qrcode.QrcodeActivity.DEFAULT_VALUE;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.INSTALLMENT_NUMBER;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TOTAL_VALUE;
import static br.com.uol.pagseguro.smartcoffee.utils.InstallmentConstants.TRANSACTION_TYPE;
import static br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_DATA;
import static br.com.uol.pagseguro.smartcoffee.utils.PreAutoKeyingConstants.PREAUTO_OPERATION;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.INSTALLMENT_TYPE_A_VISTA;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Locale;

import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.databinding.ActivityPreAutoKeyingBinding;
import br.com.uol.pagseguro.smartcoffee.payments.demoInterno.CustomDialog;

public class PreAutoKeyingActivity extends AppCompatActivity {

    private CustomDialog dialog;
    private int installment;
    private PreAutoActivity.PreAutoOperation operationType;
    private ActivityPreAutoKeyingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreAutoKeyingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        clickButtons();
    }

    private void initView() {
        binding.tvAmount.setText(getTotalAmount());
        dialog = new CustomDialog(this);
        dialog.setOnCancelListener(DialogInterface::dismiss);

        int transactionType = getIntent().getIntExtra(TRANSACTION_TYPE, 1);
        installment = getIntent().getIntExtra(INSTALLMENT_NUMBER, 1);
        operationType = (PreAutoActivity.PreAutoOperation) getIntent().getExtras().get(
                PREAUTO_OPERATION
        );

        if (operationType == PREAUTO_KEYED_CREATE || operationType == PREAUTO_KEYED) {
            binding.txtTransactionDate.setVisibility(View.GONE);
            binding.txtTransactionCode.setVisibility(View.GONE);
            binding.txtTransactionDate.setFocusable(false);
            binding.txtTransactionCode.setFocusable(false);

            if (transactionType == INSTALLMENT_TYPE_A_VISTA) {
                binding.tvInstallments.setText(getString(R.string.text_a_vista));
            } else {
                String installmentText = getString(R.string.text_a_prazo) + " " + installment + "X";
                binding.tvInstallments.setText(installmentText);
            }
        }

    }

    private String getTotalAmount() {
        Double convertedValue = (double) getIntent().getIntExtra(TOTAL_VALUE, DEFAULT_VALUE) / 100;
        NumberFormat currencyLocale = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return currencyLocale.format(convertedValue)
                .replace(
                        currencyLocale.getCurrency().getCurrencyCode(),
                        currencyLocale.getCurrency().getCurrencyCode() + ""
                );
    }

    private void clickButtons() {
        Intent returnIntent = new Intent();

        binding.btnOk.setOnClickListener(click -> {
            try {

                returnIntent.putExtra(PREAUTO_DATA, createPreAutoData());
                returnIntent.putExtra(PREAUTO_OPERATION, operationType);

                setResult(RESULT_OK, returnIntent);
                finish();
            } catch (Exception e){
                showDialog(e.getMessage());
            }
        });
        binding.btnCancel.setOnClickListener(click -> {
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        });
    }

    private PreAutoPayment createPreAutoData() throws Exception {
        PreAutoPayment preAutoPayment;

        if (operationType == PREAUTO_KEYED || operationType == PREAUTO_KEYED_CREATE) {
            preAutoPayment = new PreAutoPayment(
                    installment,
                    validateInput(binding.txtPan),
                    formatExpDate(),
                    validateInput(binding.txtCreditCardCvv),
                    "",
                    ""
            );
        } else {
            preAutoPayment = new PreAutoPayment(
                    installment,
                    validateInput(binding.txtPan),
                    formatExpDate(),
                    validateInput(binding.txtCreditCardCvv),
                    validateInput(binding.txtTransactionCode),
                    formatTransactionDate()
            );
        }
        return preAutoPayment;
    }

    private String validateInput(EditText input) throws Exception {
        String text = input.getText().toString();
        if (text.isEmpty()) {
            throw new Exception(getString(R.string.txt_fill_fields_values));
        }
        return text;
    }

    private String formatExpDate() {
        String expDate = binding.txtCreditCardExpDate.getText().toString();
        if (expDate.length() == 4) {
            expDate = expDate.substring(2) + expDate.substring(0, 2);
        }
        return expDate;
    }

    private String formatTransactionDate() throws Exception {
        String date = binding.txtTransactionDate.getText().toString();
        if (date.length() == 8) {
            String day = date.substring(0, 2);
            String month = date.substring(2, 4);
            String year = date.substring(4, 8);
            date = day + "-" + month + "-" + year + "-";
        } else {
            throw new Exception(getString(R.string.txt_fill_date_value));
        }
        return date;
    }

    private void showDialog(String message) {
        if (!dialog.isShowing()) {
            dialog.show();
        }
        dialog.setMessage(message);
    }

}
