package br.com.uol.pagseguro.smartcoffee.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import br.com.uol.pagseguro.smartcoffee.ActionResult;
import br.com.uol.pagseguro.smartcoffee.R;

public class FileHelper {

    private static final String FILE_NAME = "transactionInfo.txt";

    private static void writeToFile(String transactionInfos, Context context) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(transactionInfos);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ActionResult readFromFile(Context context) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = context.openFileInput(FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = bufferedReader.readLine();
            ActionResult actionResult = new ActionResult();
            String[] split = line.split(":");
            actionResult.setTransactionCode(split[0]);
            actionResult.setTransactionId(split.length > 1 ? split[1] : "");
            return actionResult;
        } catch (Exception e) {
            ActionResult result = new ActionResult();
            result.setMessage(context.getString(R.string.no_transaction_to_refund));
            return result;
        }
    }

    public static void writeToFile(String transactionCode, String transactionID, Context context) {
        writeToFile(transactionCode.concat(":").concat(transactionID), context);
    }
}
