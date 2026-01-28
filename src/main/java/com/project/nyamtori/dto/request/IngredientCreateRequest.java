package com.project.nyamtori.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class IngredientCreateRequest {
    @NotBlank(message = "재료를 입력해주세요.")
    private String ingredientName;

    @NotNull(message = "유통기한은 필수입니다.")
    private LocalDate ingredientDate;

    private String ingredientEtc;

    private String imgUrl;

    private int amount;

    @NotBlank(message = "카테고리를 선택해주세요.")
    private String category;

}
