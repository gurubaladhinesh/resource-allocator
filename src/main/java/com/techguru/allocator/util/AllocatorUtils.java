package com.techguru.allocator.util;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.techguru.allocator.exception.AllocatorException;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * AllocatorUtils - Util methods used across allocator
 */
public interface AllocatorUtils {

    /**
     * Validate if the json is valid
     *
     * @param fileContent the json file content as string
     * @throws AllocatorException the allocator exception
     */
    static void validate(String fileContent) throws AllocatorException {
        try {
            JsonParser.parseString(fileContent);
        } catch (JsonSyntaxException e) {
            throw new AllocatorException("Not a valid json");
        }
    }

    /**
     * Rounds the input double vale to the # of decimalPlaces
     *
     * @param value         the value
     * @param decimalPlaces the decimal places
     * @return the double
     */
    static double round(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }

    /**
     * Returns USD representation of input double value
     *
     * @param value the value
     * @return the USD representation
     */
    static String dollarValue(Double value) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }
}
