package uol.pagseguro.com.br.smartcoffee.utils;

import android.app.AlertDialog;
import android.content.Context;

public class UIFeedback {

    static AlertDialog dialog;

    private static void initDialog(Context context) {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(context).create();
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
        dialog.setCancelable(isCancelable);
        dialog.setMessage(message == null ? context.getString(resourceMessage) : message);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}
