package com.project.nyamtori.dto.response;

public record IngredientCreateResponse (
        Long ingredientId,
        String ingredientName,
        String ingredientDate,
        String ingredientEtc,
        String imgUrl,
        int amount,
        String category
) {}
