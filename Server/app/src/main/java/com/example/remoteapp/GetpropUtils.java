package com.example.remoteapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for fetching Android system properties using the getprop command.
 */
public class GetpropUtils {

    /**
     * Fetches a predefined set of commonly requested device properties.
     *
     * @return A formatted string containing selected device properties, or an error message if not found.
     */
    public static String getProp() {
        String[] selected_key = {"ro.product.brand", "ro.product.model", "ro.build.version.release", "dolby"};
        //return GetpropUtils.getFormattedProps(selected_key);
        //return  GetpropUtils.getPropsByReflection(selected_key);
        return getAllProps();
    }

    /**
     * Fetches ALL available properties using the getprop command.
     *
     * @return A raw string with all system properties.
     */
    public static String getAllProps() {
        StringBuilder results= new StringBuilder();
        Process process = null;
        try {
            process = new ProcessBuilder().command("/system/bin/getprop").redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                results.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e("GetpropUtils", "Error fetching properties", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return results.toString();
    }

    /**
     * Fetches specific properties using the getprop command (searches by keywords).
     *
     * @param keywords Array of property keys to search for.
     * @return Map where each key is a property name and each value is the property value.
     */

   /* public static Map<String, String> getProps(String... keywords) {
        Map<String, String> results = new HashMap<>();
        Process process = null;
        try {
            process = new ProcessBuilder().command("/system/bin/getprop").redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String keyword : keywords) {
                    if (line.trim().startsWith("[" + keyword + "]")) {
                        String value = extractValue(line);
                        results.put(keyword, value);
                    }
                }
                results.
            }
        } catch (IOException e) {
            Log.e("GetpropUtils", "Error fetching properties", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return results;
    }*/

    /**
     * Extracts the value part from a line outputted by the getprop command.
     * @param line A single line from getprop output.
     * @return The extracted property value, or an empty string if parsing fails.
     */
    private static String extractValue(String line) {
        int endKeyIndex = line.indexOf("]: [");
        if (endKeyIndex == -1) return "";
        return line.substring(endKeyIndex + 4, line.length() - 1);
    }

    /**
     * Fetches and formats the requested properties into a readable text block.
     *
     * @return A formatted string listing each property and its value.
     */
   /*public static String getFormattedProps(String... keywords) {
        Map<String, String> props = getProps(keywords);
        if (props.isEmpty()) {
            return "No matching properties found.";
        }
        StringBuilder builder = new StringBuilder("Device Properties:\n");
        for (Map.Entry<String, String> entry : props.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return builder.toString();
    }*/


    /**
     * Fetches specific properties using reflection.
     *
     * @param keys Array of property keys to fetch.
     * @return A formatted string listing the properties and their values.
     */
    public static String getPropsByReflection(String... keys) {
        StringBuilder builder = new StringBuilder("Device Properties:\n");
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            java.lang.reflect.Method getMethod = systemProperties.getMethod("get", String.class);

            for (String key : keys) {
                String value = (String) getMethod.invoke(null, key);
                if (value == null || value.isEmpty()) {
                    value = "N/A";
                }
                builder.append(key).append(": ").append(value).append("\n");
            }

        } catch (Exception e) {
            Log.e("GetpropUtils", "Reflection failed", e);
            return "Error fetching properties by reflection.";
        }
        return builder.toString();
    }
}
