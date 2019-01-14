package uol.pagseguro.com.br.smartcoffee.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

public class UIFeedback {

    static AlertDialog sDialog;
    static ProgressDialog sProgressDialog;

    private static void initDialog(Context context) {
        if (sDialog == null) {
            sDialog = new AlertDialog.Builder(context).create();
        }
    }

    public static void showDialog(Context context, String message, Boolean isCancelable) {
        showDialog(context, message, 0, isCancelable);
    }

    public static void showDialog(Context context, String message) {
        showDialog(context, message, 0, true);
    }

    public static void showDialog(Context context, int message) {
        showDialog(context, null, message, true);
    }

    public static void showDialog(Context context, String message, int resourceMessage, Boolean isCancelable) {
        initDialog(context);
        sDialog.setCancelable(isCancelable);
        sDialog.setMessage(message == null ? context.getString(resourceMessage) : message);
        if (!sDialog.isShowing()) {
            sDialog.show();
        }
    }

    public static ProgressDialog getProgress(Context context) {
        if (sProgressDialog == null) {
            sProgressDialog = new ProgressDialog(context);
            sProgressDialog.setMessage("Aguarde...");
        }
        return sProgressDialog;
    }

    public static void showProgress(Context context) {
        getProgress(context).show();
    }

    public static void dismissProgress() {
        sProgressDialog.dismiss();
    }
}
