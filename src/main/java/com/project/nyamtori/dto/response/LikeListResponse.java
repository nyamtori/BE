package com.project.nyamtori.dto.response;

import com.project.nyamtori.domain.Like;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class LikeListResponse {
    private Long likeId;
    private Long recipeId;
    private String recipeFood; //title 대신
    private int cookTime;
    private LocalDateTime likedAt;

    public static LikeResponse from(Like like) {
        return LikeResponse.builder()
                .likeId(like.getLikeId())
                .recipeId(like.getRecipe().getRecipeId())
                .recipeFood(like.getRecipe().getFood())
                .cookTime(like.getRecipe().getCookTime())
                .likedAt(like.getLikedAt())
                .build();
    }
}
