package com.project.nyamtori.controller;

import com.project.nyamtori.dto.request.IngredientCreateRequest;
import com.project.nyamtori.dto.response.IngredientCreateResponse;
import com.project.nyamtori.dto.response.IngredientDetailResponse;
import com.project.nyamtori.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    // 로그인 완성 전
    @PostMapping
    @Operation(
            summary = "재료 직접 입력",
            description = "바코드 인식 후 추가 정보를 입력하거나, 처음부터 수기로 재료를 입력합니다."
    )
    public ResponseEntity<IngredientCreateResponse> createIngredient(
            @Valid @RequestBody IngredientCreateRequest req
    ) {
        log.info("[POST] /api/v1/ingredients name={}", req.getIngredientName());

        return ResponseEntity.ok(ingredientService.createIngredient(req));
    }

    @GetMapping
    @Operation(
            summary = "재료 단건 조회",
            description = "재료의 세부 내역을 조회합니다."
    )
    public ResponseEntity<IngredientDetailResponse> readIngredient(@PathVariable Long ingredientId) {
        IngredientDetailResponse resp = ingredientService.readIngredient(ingredientId);
        return ResponseEntity.ok(resp);
    }
}
