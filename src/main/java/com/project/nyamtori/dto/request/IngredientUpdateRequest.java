package com.project.nyamtori.dto.request;

import lombok.Getter;

@Getter
public class IngredientUpdateRequest {
    private String ingredientName;
    private String ingredientDate;
    private String ingredientEtc;
    private String imgUrl;
    private Integer amount;
    private String location;
}
