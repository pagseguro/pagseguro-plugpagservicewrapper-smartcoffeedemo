package br.com.uol.pagseguro.smartcoffee.demoInterno;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.uol.pagseguro.smartcoffee.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomDialog extends Dialog {

    @BindView(R.id.textview_message)
    TextView mTextViewMessage;

    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
    }

    public void setMessage(String message) {
        mTextViewMessage.setText(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.btn_cancel)
    public void mCancelBtnClicked() {
        cancel();
    }
}
