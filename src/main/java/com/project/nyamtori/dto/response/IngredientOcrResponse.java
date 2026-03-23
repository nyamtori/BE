package com.project.nyamtori.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IngredientOcrResponse {

    private String name;
    private String expiryDate;
    private int quantity;
    private String memo;
    private String storage;
    private String rawText;
}