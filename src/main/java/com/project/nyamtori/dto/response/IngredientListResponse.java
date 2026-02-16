package com.project.nyamtori.dto.response;

import com.project.nyamtori.domain.Ingredient;

public record IngredientListResponse(
        Long ingredientId,
        String ingredientName,
        String imgUrl,
        String location,
        String ingredientDate,
        int amount
) {
    public static IngredientListResponse from(Ingredient entity) {
        return new IngredientListResponse(
                entity.getIngredientId(),
                entity.getIngredientName(),
                entity.getImgUrl(),
                entity.getLocation(),
                entity.getIngredientDate() != null ? entity.getIngredientDate().toString() : null,
                entity.getAmount()
        );
    }
}