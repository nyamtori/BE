package com.project.nyamtori.service;

import org.springframework.stereotype.Component;

@Component
public class IngredientMatcher {
    public String extract(String productName, String foodType) {
        String source = safeLower(productName) + " " + safeLower(foodType);

        if (containsAny(source, "양배추")) return "양배추";
        if (containsAny(source, "배추")) return "배추";
        if (containsAny(source, "우유", "milk")) return "우유";
        if (containsAny(source, "계란", "달걀", "특란", "대란", "란", "egg")) return "계란";
        if (containsAny(source, "두부")) return "두부";
        if (containsAny(source, "된장")) return "된장";
        if (containsAny(source, "고추장")) return "고추장";
        if (containsAny(source, "간장")) return "간장";
        if (containsAny(source, "양파", "onion", "어니언")) return "양파";
        if (containsAny(source, "대파", "쪽파", "실파")) return "파";
        if (containsAny(source, "마늘")) return "마늘";
        if (containsAny(source, "감자")) return "감자";
        if (containsAny(source, "당근")) return "당근";
        if (containsAny(source, "참치")) return "참치";
        if (containsAny(source, "김치")) return "김치";
        if (containsAny(source, "라면")) return "라면";

        return productName;
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

}
