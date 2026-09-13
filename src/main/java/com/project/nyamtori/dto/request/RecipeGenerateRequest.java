package com.project.nyamtori.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeGenerateRequest {
    private List<Long> ingredientIds;
    private Integer cookTime;

}
