package br.com.uol.pagseguro.smartcoffee.payments.installments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.jetbrains.annotations.NotNull;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.smartcoffee.databinding.DialogInputValuePreAutoBinding;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.DismissListenerEffectivate;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;
import butterknife.ButterKnife;

public class CustomDialogPreAutoValue extends DialogFragment {

    private DismissListenerEffectivate mOnDismissListener;
    private Integer mValueTotalCreatePreAuto;
    private PlugPagTransactionResult mPlugPagTransactionResult;
    private DialogInputValuePreAutoBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogInputValuePreAutoBinding.inflate(getLayoutInflater());
        View rootview = binding.getRoot();
        ButterKnife.bind(this, rootview);
        return rootview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvValuePreAuto.setText(Utils.getFormattedValue(Double.valueOf(mValueTotalCreatePreAuto)));
        binding.edittextInput.addTextChangedListener(getWatcher());
        clickButtons();
    }

    private void clickButtons() {
        binding.buttonConfirm.setOnClickListener(click -> {
            String value = binding.edittextInput.getText().toString();

            if (value.isEmpty()) dismiss();

            final String amount = binding.edittextInput.getText() != null ?
                    binding.edittextInput.getText().toString().replaceAll("[^0-9]*", "") : "";
            mOnDismissListener.onDismissEffectivate(amount, mPlugPagTransactionResult);
            dismiss();
        });
    }

    @NotNull
    private TextWatcher getWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String typed = Utils.onlyDigits(charSequence.toString());

                if (!TextUtils.isEmpty(typed)) {
                    binding.edittextInput.removeTextChangedListener(this);
                    String convertedString = Utils.getFormattedValue(Double.parseDouble(typed));
                    binding.edittextInput.setText(convertedString);
                    binding.edittextInput.setSelection(convertedString.length());
                    binding.edittextInput.addTextChangedListener(this);
                } else {
                    binding.edittextInput.setText("0");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        };
    }

    public void setOnDismissListener(DismissListenerEffectivate onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void setPlugPagTransactionResult(PlugPagTransactionResult plugPagTransactionResult) {
        mPlugPagTransactionResult = plugPagTransactionResult;
    }

    public void setValueTotalCreatePreAuto(Integer value) {
        mValueTotalCreatePreAuto = value;
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;

        getDialog().getWindow().setAttributes(lp);
    }
}
