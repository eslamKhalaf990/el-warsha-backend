package com.warsha.erp.services;

import java.util.*;

public class EgyptGovernorates {

    private static final Map<String, Double> SHIPPING_RATES = new HashMap<>();

    static {
        // --- Zone 1: Metro & Delta (Lower Cost - 55 EGP) ---
        SHIPPING_RATES.put("القاهرة", 80.0);
        SHIPPING_RATES.put("الجيزة", 80.0);
        SHIPPING_RATES.put("القليوبية", 100.0);
        SHIPPING_RATES.put("المنوفية", 100.0);
        SHIPPING_RATES.put("الشرقية", 100.0);
        SHIPPING_RATES.put("الغربية", 100.0);

        // --- Zone 2: Coastal, Canal & North Upper Egypt (65 EGP) ---
        SHIPPING_RATES.put("الإسكندرية", 90.0);
        SHIPPING_RATES.put("البحيرة", 100.0);
        SHIPPING_RATES.put("الدقهلية", 100.0);
        SHIPPING_RATES.put("كفر الشيخ", 100.0);
        SHIPPING_RATES.put("الإسماعيلية", 100.0);
        SHIPPING_RATES.put("السويس", 100.0);
        SHIPPING_RATES.put("بورسعيد", 100.0);
        SHIPPING_RATES.put("الفيوم", 110.0);
        SHIPPING_RATES.put("بني سويف", 110.0);
        SHIPPING_RATES.put("المنيا", 110.0);
        SHIPPING_RATES.put("دمياط", 100.0);
        SHIPPING_RATES.put("دمياط الجديدة", 100.0);

        // --- Zone 3: Middle Upper Egypt & Sinai (80 EGP) ---
        SHIPPING_RATES.put("أسيوط", 120.0);
        SHIPPING_RATES.put("شمال سيناء", 130.0);
        SHIPPING_RATES.put("جنوب سيناء", 130.0);

        // --- Zone 4: Remote & Deep South (90 EGP) ---
        SHIPPING_RATES.put("سوهاج", 120.0);
        SHIPPING_RATES.put("البحر الأحمر", 120.0);
        SHIPPING_RATES.put("مطروح", 130.0);

        // --- Zone 5: Furthest South (100 EGP) ---
        SHIPPING_RATES.put("قنا", 120.0);
        SHIPPING_RATES.put("الأقصر", 120.0);
        SHIPPING_RATES.put("أسوان", 120.0);

        // --- Zone 6: Special (105 EGP) ---
        SHIPPING_RATES.put("الوادي الجديد", 130.0);
    }

    public static boolean isValid(String city) {
        if (city == null || city.trim().isEmpty()) return false;
        return SHIPPING_RATES.containsKey(normalize(city));
    }

    public static double getDeliveryPrice(String city) {
        if (!isValid(city)) {
            // Fallback or throw error
            throw new IllegalArgumentException("المحافظة غير موجودة في قائمة الأسعار: " + city);
        }
        return SHIPPING_RATES.get(normalize(city));
    }

    // Normalization handles spelling variations (important for Arabic)
    private static String normalize(String input) {
        if (input == null) return "";
        String trimmed = input.trim();

        if (trimmed.endsWith("ه") && !trimmed.equals("الله")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1) + "ة";
        }

        // Handle 'ال' prefix variations or specific common typos
        if (trimmed.equals("جيزة")) return "الجيزة";
        if (trimmed.equals("الاقصر")) return "الأقصر";
        if (trimmed.equals("اسوان")) return "أسوان";
        if (trimmed.equals("اسيوط")) return "أسيوط";
        if (trimmed.equals("الاسكندرية") || trimmed.equals("الاسكندريه")) return "الإسكندرية";
        if (trimmed.equals("الاسماعيلية") || trimmed.equals("الاسماعيليه")) return "الإسماعيلية";

        return trimmed;
    }
}