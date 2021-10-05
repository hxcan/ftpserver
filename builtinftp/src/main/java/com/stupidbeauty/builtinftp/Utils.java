package com.stupidbeauty.builtinftp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Utils {
    public static final String SHEL_EXECUTE_ERROR = "SHEL_EXECUTE_ERROR";
    private static final String TAG="Utils"; //!< 输出调试信息时使用的标记。陈欣

    public static String shellExec(String cmdCommand) {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            final Process process = Runtime.getRuntime().exec(cmdCommand);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String lines = bufferedReader.lines().map(line -> line + "\n").collect(Collectors.joining());

            String line;
//            bufferedReader.
            while ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, "line:" + line); // Debug.
                stringBuilder.append(line);
                stringBuilder.append("\n");
//                stringBuilder.
            }

        } catch (Exception e) {
            return SHEL_EXECUTE_ERROR;
        }
        return stringBuilder.toString();
    }
}
