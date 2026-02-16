package com.project.nyamtori.dto.response;

import com.project.nyamtori.domain.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
@AllArgsConstructor
public class IngredientDetailResponse {
    Long ingredientId;
    String ingredientName;
    String ingredientDate;
    String ingredientEtc;
    String imgUrl;
    int amount;
    String location;

    public static IngredientDetailResponse from(Ingredient entity) {
        return IngredientDetailResponse.builder()
                .ingredientId(entity.getIngredientId())
                .ingredientName(entity.getIngredientName())
                .ingredientDate(entity.getIngredientDate() != null ?
                        entity.getIngredientDate().toString() : null)
                .ingredientEtc(entity.getIngredientEtc())
                .imgUrl(entity.getImgUrl())
                .amount(entity.getAmount())
                .location(entity.getLocation())
                .build();
    }
}
