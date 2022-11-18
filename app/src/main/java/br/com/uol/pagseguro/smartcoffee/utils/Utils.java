package br.com.uol.pagseguro.smartcoffee.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;

public class Utils {

    public static final String VALUE_NULL_OR_EMPTY = "XXX";

    private static int countPassword = 0;

    public static String isNotNullOrEmpty(String value) {
        if (value != null && !value.isEmpty()) {
            return value;
        } else {
            return VALUE_NULL_OR_EMPTY;
        }
    }

    public static String onlyDigits(String textValue) {
        return textValue.replaceAll("[^\\d]", "");
    }

    public static String getFormattedValue(Double value) {
        double converted = value / 100;
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(converted);
    }

    public static Integer getValue(String value) {
        if (value.isEmpty()) return 0;

        return Integer.valueOf(value.replaceAll("[^0-9]*", ""));
    }

    public static byte[] convertString2Bytes(String content) {
        byte[] ret = new byte[16];
        byte[] buf = content.getBytes(StandardCharsets.UTF_8);
        int retLen = ret.length;
        int bufLen = buf.length;
        boolean b = retLen > bufLen;

        for (int i = 0; i < retLen; i++) {
            if (b && i >= bufLen) {
                ret[i] = 0;
                continue;
            }
            ret[i] = buf[i];
        }
        return ret;
    }

    private List<Integer> getSectorTrailerBlocks() {
        final List<Integer> ret = new ArrayList<>();

        for (int i = 7; i < 36; i += 4){
            ret.add(i);
        }

        return ret;
    }

    private byte[] buildDataAccess(@NonNull byte[] keyA, @NonNull byte[] permissions, @Nullable byte[] keyB){
        byte[] data = new byte[16];
        System.arraycopy(keyA, 0, data, 0, 6);
        System.arraycopy(permissions, 0, data, 6, 4);

        if (keyB != null) {
            System.arraycopy(keyB, 0, data, 10, 6);
        }

        return data;
    }

    public static String checkMessagePassword(int eventCode, int value) {
        StringBuilder strPassword = new StringBuilder();

        if (eventCode == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
            countPassword++;
        }
        if (eventCode == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
            countPassword = 0;
        }

        for (int count = countPassword; count > 0; count--) {
            strPassword.append("*");
        }

        return String.format("VALOR: %.2f\nSENHA: %s", (value / 100.0), strPassword);
    }

    public static String checkMessage(String message) {
        if (message != null && message.contains("SENHA") && !message.contains("INCORRETA")) {
            String[] strings = message.split("SENHA");
            return strings[0].trim();
        }

        return message;
    }

}
