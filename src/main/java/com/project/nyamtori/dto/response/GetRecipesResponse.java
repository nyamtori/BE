package com.project.nyamtori.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetRecipesResponse {
    private List<RecipeInfo> data;


    public record RecipeInfo(
            Long recipeId,
            String title,
            Integer cookTime,
            Boolean liked
    ){}
}
