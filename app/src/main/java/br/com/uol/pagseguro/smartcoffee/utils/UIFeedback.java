package br.com.uol.pagseguro.smartcoffee.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UIFeedback {

    static AlertDialog sDialog;
    static ProgressDialog sProgressDialog;

    private static void initDialog(Context context) {
        if (sDialog == null) {
            sDialog = new AlertDialog.Builder(context).create();
        }
    }

    public static void showDialog(Context context, int message) {
        showDialog(context, null, message, true, null);
    }

    public static void showDialog(Context context, int message, Boolean isCancelable) {
        showDialog(context, null, message, isCancelable, null);
    }

    public static void showDialog(Context context, String message) {
        showDialog(context, message, 0, true, null);
    }

    public static void showDialog(Context context, String message, DialogInterface.OnCancelListener cancelListener) {
        showDialog(context, message, 0, true, cancelListener);
    }

    public static void showDialog(Context context, String message, int resourceMessage, Boolean isCancelable, DialogInterface.OnCancelListener cancelListener) {
        initDialog(context);
        sDialog.setCancelable(isCancelable);
        sDialog.setMessage(message == null ? context.getString(resourceMessage) : message);
        sDialog.setOnCancelListener(cancelListener);
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
        if (sProgressDialog != null) {
            sProgressDialog.dismiss();
            sProgressDialog = null;
        }
    }

    public static void dismissDialog() {
        if (sDialog != null) {
            sDialog.dismiss();
            sDialog = null;
        }
    }

    public static void dismiss() {
        dismissProgress();
        dismissDialog();
    }
}
