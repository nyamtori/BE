package com.project.nyamtori.repository;

import com.project.nyamtori.domain.Like;
import com.project.nyamtori.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findAllByUser(User user);
    Optional<Like> findByLikeIdAndUser(Long likeId, User user);
    boolean existsByUserAndRecipe_RecipeId(User user, Long recipeId);
}
