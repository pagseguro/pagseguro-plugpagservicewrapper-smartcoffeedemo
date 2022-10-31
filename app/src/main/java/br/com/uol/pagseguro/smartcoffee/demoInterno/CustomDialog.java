package br.com.uol.pagseguro.smartcoffee.demoInterno;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import br.com.uol.pagseguro.smartcoffee.databinding.CustomDialogBinding;

public class CustomDialog extends Dialog {

    public CustomDialog(@NonNull Context context) {
        super(context);
    }
    private CustomDialogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CustomDialogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCanceledOnTouchOutside(false);
        clickButtons();
    }

    public void setMessage(String message) {
        binding.textviewMessage.setText(message);
    }

    private void clickButtons() {
        binding.btnCancel.setOnClickListener(click ->
                cancel()
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
