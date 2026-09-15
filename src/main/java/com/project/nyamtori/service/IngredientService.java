package com.project.nyamtori.service;

import com.project.nyamtori.dto.request.IngredientCreateRequest;
import com.project.nyamtori.domain.Ingredient;
import com.project.nyamtori.domain.User;
import com.project.nyamtori.dto.request.IngredientUpdateRequest;
import com.project.nyamtori.dto.response.IngredientCreateResponse;
import com.project.nyamtori.dto.response.IngredientDetailResponse;
import com.project.nyamtori.dto.response.IngredientListResponse;
import com.project.nyamtori.repository.IngredientRepository;
import com.project.nyamtori.repository.UserRepository;
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
    private final UserRepository userRepository;

    private User getUser(Long kakaoId) {
        return userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public IngredientCreateResponse createIngredient(IngredientCreateRequest req, Long kakaoId) {

        int amount = req.getAmount();
        if (amount < 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        User user = getUser(kakaoId);

        Ingredient ingredient = new Ingredient();
        ingredient.applyCreate(
                req.getIngredientName(),
                req.getIngredientDate(),
                req.getIngredientEtc(),
                req.getImgUrl(),
                req.getAmount(),
                req.getLocation(),
                user
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
    public IngredientDetailResponse readIngredient(Long ingredientId, Long kakaoId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 재료입니다."));

        requireOwner(ingredient, kakaoId);

        return IngredientDetailResponse.from(ingredient);
    }

    // 재료 목록 조회 -> 리스트 (로그인한 유저 본인 소유만)
    @Transactional(readOnly = true)
    public List<IngredientListResponse> readIngredientList(Long kakaoId) {
        User user = getUser(kakaoId);

        return ingredientRepository.findAllByUser(user).stream()
                .map(IngredientListResponse::from)
                .toList();
    }

    // 부분 수정
    public IngredientDetailResponse updateIngredient (
            Long ingredientId,
            IngredientUpdateRequest request,
            Long kakaoId
    ) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료가 없습니다."));

        requireOwner(ingredient, kakaoId);

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

    public void deleteIngredient(Long ingredientId, Long kakaoId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(()-> new IllegalArgumentException("해당 재료를 찾을 수 없습니다."));

        requireOwner(ingredient, kakaoId);

        ingredientRepository.delete(ingredient);
    }

    private void requireOwner(Ingredient ingredient, Long kakaoId) {
        User owner = ingredient.getUser();
        if (owner == null || !owner.getKakaoId().equals(String.valueOf(kakaoId))) {
            throw new IllegalArgumentException("본인이 등록한 재료만 접근할 수 있습니다.");
        }
    }

}
