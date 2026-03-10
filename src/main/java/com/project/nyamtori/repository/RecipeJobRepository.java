package com.project.nyamtori.repository;

import com.project.nyamtori.domain.RecipeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeJobRepository extends JpaRepository<RecipeJob, Long> {
}
