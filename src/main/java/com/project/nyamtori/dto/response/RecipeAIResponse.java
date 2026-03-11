package com.project.nyamtori.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeAIResponse {
    private List<RecipeItem> recipes;

    public record RecipeItem(
            String title,
            List<IngredientItem> ingredients,
            List<String> steps
    ){}

    public record IngredientItem(
            String name,
            String amount
    ){}
}
