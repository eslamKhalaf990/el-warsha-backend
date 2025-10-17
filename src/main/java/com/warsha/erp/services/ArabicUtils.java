package com.warsha.erp.services;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;

public class ArabicUtils {
    public static String reshapeArabic(String input) {
        try {
            ArabicShaping arabicShaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
            String shaped = arabicShaping.shape(input);

            Bidi bidi = new Bidi(input, Bidi.DIRECTION_RIGHT_TO_LEFT);
            return shaped;
        } catch (ArabicShapingException e) {
            e.printStackTrace();
            return input;
        }
    }
}
