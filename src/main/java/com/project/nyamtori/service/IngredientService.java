package com.project.nyamtori.service;

import com.project.nyamtori.dto.request.IngredientCreateRequest;
import com.project.nyamtori.domain.Ingredient;
import com.project.nyamtori.dto.response.IngredientCreateResponse;
import com.project.nyamtori.repository.IngredientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public IngredientCreateResponse createIngredient(IngredientCreateRequest req) {

        // 1. 중복 이름 방지
        if (ingredientRepository.existsByIngredientName(req.getIngredientName())) {
            throw new IllegalArgumentException("이미 존재하는 재료입니다.");
        }

        // 2. 카테고리 항목 없으면 ETC 처리
        String category = req.getCategory();
        if (category == null || category.isBlank()) {
            category = "ETC";
        }

        // 3. 엔티티 생성
        Ingredient ingredient = new Ingredient();
        ingredient.applyCreate(
                req.getIngredientName(),
                req.getIngredientDate(),
                req.getIngredientEtc(),
                req.getImgUrl(),
                category
        );

        Ingredient saved = ingredientRepository.save(ingredient);

        return new IngredientCreateResponse(
                saved.getIngredientId(),
                saved.getIngredientName(),
                saved.getIngredientDate().toString(),
                saved.getIngredientEtc(),
                saved.getImgUrl(),
                saved.getCategory()
        );
    }

}
