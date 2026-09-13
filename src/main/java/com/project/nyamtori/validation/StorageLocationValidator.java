package com.project.nyamtori.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class StorageLocationValidator implements ConstraintValidator<ValidStorageLocation, String> {

    private static final Set<String> ALLOWED_LOCATIONS = Set.of("냉장", "냉동", "실온");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // 필수 여부는 @NotBlank 등 별도 애노테이션이 담당
        }
        return ALLOWED_LOCATIONS.contains(value);
    }
}
