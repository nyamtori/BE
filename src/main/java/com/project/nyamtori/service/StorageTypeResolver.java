package com.project.nyamtori.service;

import org.springframework.stereotype.Component;

@Component
public class StorageTypeResolver {

    public String resolve(String ingredientName) {
        String source = safeLower(ingredientName);

        if (containsAny(source, "우유", "계란", "두부", "치즈", "요거트", "햄", "베이컨", "김치")) {
            return "냉장";
        }

        if (containsAny(source, "만두", "냉동", "새우", "오징어", "고등어")) {
            return "냉동";
        }

        return "상온";
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