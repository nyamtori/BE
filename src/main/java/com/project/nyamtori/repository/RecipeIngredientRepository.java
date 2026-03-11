package com.project.nyamtori.repository;

import com.project.nyamtori.domain.Recipe;
import com.project.nyamtori.domain.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    List<RecipeIngredient> findByRecipeRecipeId(Long recipeId);
}
