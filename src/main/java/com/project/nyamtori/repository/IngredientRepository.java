package com.project.nyamtori.repository;

import com.project.nyamtori.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    boolean existsByIngredientName(String ingredientName);
    Optional<Ingredient> findByIngredientName(String ingredientName);
}
