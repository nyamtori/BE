package com.project.nyamtori.service;

import com.project.nyamtori.dto.request.IngredientCreateRequest;
import com.project.nyamtori.domain.Ingredient;
import com.project.nyamtori.dto.request.IngredientUpdateRequest;
import com.project.nyamtori.dto.response.IngredientCreateResponse;
import com.project.nyamtori.dto.response.IngredientDetailResponse;
import com.project.nyamtori.dto.response.IngredientListResponse;
import com.project.nyamtori.repository.IngredientRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public IngredientCreateResponse createIngredient(IngredientCreateRequest req) {

        int amount = req.getAmount();
        if (amount < 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.applyCreate(
                req.getIngredientName(),
                req.getIngredientDate(),
                req.getIngredientEtc(),
                req.getImgUrl(),
                req.getAmount(),
                req.getLocation()
        );

        Ingredient saved = ingredientRepository.save(ingredient);

        return new IngredientCreateResponse(
                saved.getIngredientId(),
                saved.getIngredientName(),
                saved.getIngredientDate().toString(),
                saved.getIngredientEtc(),
                saved.getImgUrl(),
                saved.getAmount(),
                saved.getLocation()
        );
    }

    // 재료 단건 조회
    @Transactional(readOnly = true)
    public IngredientDetailResponse readIngredient(Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 재료입니다."));

        return IngredientDetailResponse.from(ingredient);
    }

    // 재료 목록 조회 -> 리스트
    @Transactional(readOnly = true)
    public List<IngredientListResponse> readIngredientList() {
        return ingredientRepository.findAll().stream()
                .map(IngredientListResponse::from)
                .toList();
    }

    // 부분 수정
    public IngredientDetailResponse updateIngredient (
            Long ingredientId,
            IngredientUpdateRequest request
    ) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료가 없습니다."));


        ingredient.update(
                request.getIngredientName(),
                request.getIngredientDate(),
                request.getIngredientEtc(),
                request.getImgUrl(),
                request.getAmount(),
                request.getLocation()
        );

        return IngredientDetailResponse.from(ingredient);
    }

    public void deleteIngredient(Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(()-> new IllegalArgumentException("해당 재료를 찾을 수 없습니다."));

        ingredientRepository.delete(ingredient);
    }

}
