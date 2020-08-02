package com.techguru.allocator.util;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.techguru.allocator.exception.AllocatorException;

import java.text.NumberFormat;
import java.util.Locale;

public interface AllocatorUtils {

    static void validate(String fileContent) throws AllocatorException {
        try {
            JsonParser.parseString(fileContent);
        } catch (JsonSyntaxException e) {
            throw new AllocatorException("Not a valid json");
        }
    }

    static double round(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }

    static String dollarValue(Double value) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }
}
