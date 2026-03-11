package com.project.nyamtori.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RecipeResponse {
    private Long recipeId;
    private String title;
    private Integer cookTime;
    private List<Ingredients> ingredients;
    private List<String> steps;
    private Boolean liked;

    public record Ingredients(
            Long ingredientId,
            String name,
            String amount
    ){}

}
