package com.project.nyamtori.controller;

import com.project.nyamtori.dto.request.IngredientCreateRequest;
import com.project.nyamtori.dto.request.IngredientUpdateRequest;
import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import com.project.nyamtori.dto.response.IngredientCreateResponse;
import com.project.nyamtori.dto.response.IngredientDetailResponse;
import com.project.nyamtori.dto.response.IngredientListResponse;
import com.project.nyamtori.service.BarcodeLookupService;
import com.project.nyamtori.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;
    private final BarcodeLookupService barcodeLookupService;

    // 로그인 완성 전
    @PostMapping ("/manual")
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

    @PostMapping("/lookup")
    @Operation(
            summary = "바코드 조회 및 유통기한 추출",
            description = "바코드를 인식하고, 유통기한을 이미지에서 추출합니다."
    )
    public ResponseEntity<BarcodeLookupResponse> lookupIngredient(
            @RequestParam String barcode,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        BufferedImage img = ImageIO.read(image.getInputStream());
        BarcodeLookupResponse response = barcodeLookupService.lookup(barcode, img);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ingredientId}")
    @Operation(
            summary = "재료 단건 조회",
            description = "재료의 세부 내역을 조회합니다."
    )
    public ResponseEntity<IngredientDetailResponse> readIngredientId(@PathVariable Long ingredientId) {
        IngredientDetailResponse resp = ingredientService.readIngredient(ingredientId);
        return ResponseEntity.ok(resp);
    }


    @GetMapping
    @Operation(
            summary = "재료 목록 조회",
            description = "등록된 재료들을 전체 조회합니다."
    )
    public ResponseEntity<List<IngredientListResponse>> readAllIngredients() {
        List<IngredientListResponse> list= ingredientService.readIngredientList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{ingredientId}")
    @Operation(
            summary = "재료 수정",
            description = "재료 정보를 부분 수정합니다."
    )
    public ResponseEntity<IngredientDetailResponse> updateIngredient(
            @PathVariable Long ingredientId,
            @RequestBody IngredientUpdateRequest request
            ) {
        IngredientDetailResponse response = ingredientService.updateIngredient(ingredientId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ingredientId}")
    @Operation(
            summary = "재료 삭제",
            description = "재료를 삭제합니다."
    )
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long ingredientId) {
        ingredientService.deleteIngredient(ingredientId);
        return ResponseEntity.noContent().build();
    }
}
