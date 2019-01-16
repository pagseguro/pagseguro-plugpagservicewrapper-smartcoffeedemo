package uol.pagseguro.com.br.smartcoffee.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import uol.pagseguro.com.br.smartcoffee.ActionResult;

public class FileHelper {

    private static void writeToFile(String transactionInfos, Context context) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput("transactionInfo" + ".txt", Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(transactionInfos);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ActionResult readFromFile(Context context) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput("transactionInfo" + ".txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = bufferedReader.readLine();
            ActionResult actionResult = new ActionResult();
            actionResult.setTransactionCode(line.split(":")[0]);
            actionResult.setTransactionId(line.split(":")[1]);
            return actionResult;
        } catch (IOException e) {
            e.printStackTrace();
            return new ActionResult();
        }
    }

    public static void writeToFile(String transactionCode, String transactionID, Context context) {
        writeToFile(transactionCode.concat(":").concat(transactionID), context);
    }
}
