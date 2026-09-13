package com.project.nyamtori.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.nyamtori.validation.ValidStorageLocation;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class IngredientUpdateRequest {
    private String ingredientName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ingredientDate;
    private String ingredientEtc;
    private String imgUrl;
    private Integer amount;
    @ValidStorageLocation
    private String location;
}
