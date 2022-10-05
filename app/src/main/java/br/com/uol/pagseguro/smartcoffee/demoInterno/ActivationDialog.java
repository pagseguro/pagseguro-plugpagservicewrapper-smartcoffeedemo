package br.com.uol.pagseguro.smartcoffee.demoInterno;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import br.com.uol.pagseguro.smartcoffee.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivationDialog extends DialogFragment {

    @BindView(R.id.edittext_input)
    TextInputEditText mTextInputEditText;

    @OnClick(R.id.button_confirm)
    public void onConfirmClicked() {
        mOnDismissListener.onDismiss(mTextInputEditText.getText() != null ? mTextInputEditText.getText().toString() : "");
        dismiss();
    }

    private DismissListener mOnDismissListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.dialog_input, container, false);
        ButterKnife.bind(this, rootview);
        return rootview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setOnDismissListener(DismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
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
