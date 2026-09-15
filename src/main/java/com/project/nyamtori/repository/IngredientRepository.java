package com.project.nyamtori.repository;

import com.project.nyamtori.domain.Ingredient;
import com.project.nyamtori.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    boolean existsByIngredientName(String ingredientName);
    Optional<Ingredient> findByIngredientName(String ingredientName);
    List<Ingredient> findAllByUser(User user);
}
