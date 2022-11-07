package br.com.uol.pagseguro.smartcoffee.payments.demoInterno;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import br.com.uol.pagseguro.smartcoffee.databinding.DialogInputBinding;

public class ActivationDialog extends DialogFragment {

    private DismissListener mOnDismissListener;
    private DialogInputBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogInputBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickButtons();
    }

    private void clickButtons() {
        binding.buttonConfirm.setOnClickListener(
                click -> {
                    mOnDismissListener.onDismiss(
                            binding.textinputlayout.getEditText().getText() != null ?
                                    binding.textinputlayout.getEditText().getText().toString() : ""
                    );
                    dismiss();
                }
        );
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
