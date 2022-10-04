package br.com.uol.pagseguro.smartcoffee.payments.installments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.smartcoffee.R;
import br.com.uol.pagseguro.smartcoffee.payments.preauto.DismissListenerEffectivate;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomDialogPreAutoValue extends DialogFragment {

    @BindView(R.id.edittext_input) TextInputEditText mTextInputEditText;
    @BindView(R.id.tv_value_pre_auto) TextView mTextValuePreAuto;

    private DismissListenerEffectivate mOnDismissListener;
    private Integer mValueTotalCreatePreAuto;
    private PlugPagTransactionResult mPlugPagTransactionResult;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.dialog_input_value_pre_auto, container, false);
        ButterKnife.bind(this, rootview);
        return rootview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextValuePreAuto.setText(Utils.getFormattedValue(Double.valueOf(mValueTotalCreatePreAuto)));
        mTextInputEditText.addTextChangedListener(getWatcher());
    }

    @OnClick(R.id.button_confirm)
    public void onConfirmClicked() {
        String value = mTextInputEditText.getText().toString();

        if (value.isEmpty()) dismiss();

        final String amount = mTextInputEditText.getText() != null ?
                mTextInputEditText.getText().toString().replaceAll("[^0-9]*", "") : "";
        mOnDismissListener.onDismissEffectivate(amount, mPlugPagTransactionResult);
        dismiss();
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
                    mTextInputEditText.removeTextChangedListener(this);
                    String convertedString = Utils.getFormattedValue(Double.parseDouble(typed));
                    mTextInputEditText.setText(convertedString);
                    mTextInputEditText.setSelection(convertedString.length());
                    mTextInputEditText.addTextChangedListener(this);
                } else {
                    mTextInputEditText.setText("0");
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
