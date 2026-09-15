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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


    @PostMapping ("/manual")
    @Operation(
            summary = "재료 직접 입력",
            description = "바코드 인식 후 추가 정보를 입력하거나, 처음부터 수기로 재료를 입력합니다."
    )
    public ResponseEntity<IngredientCreateResponse> createIngredient(
            @Valid @RequestBody IngredientCreateRequest req,
            @AuthenticationPrincipal Long kakaoId
    ) {
        log.info("[POST] /api/v1/ingredients name={}", req.getIngredientName());

        return ResponseEntity.ok(ingredientService.createIngredient(req, kakaoId));
    }

    @PostMapping(value = "/lookup", consumes = "multipart/form-data")
    @Operation(
            summary = "바코드 사진 / 제품 사진 / 둘 다로 조회",
            description = "바코드 사진에서 바코드를 인식하고, 제품 사진에서 유통기한을 추출합니다. 둘 중 하나만 보내도 됩니다."
    )
    public ResponseEntity<BarcodeLookupResponse> lookupIngredient(
            @RequestPart(value = "barcodeImage", required = false) MultipartFile barcodeImage,
            @RequestPart(value = "productImage", required = false) MultipartFile productImage
    ) throws IOException {
        BufferedImage barcodeImg = toBufferedImage(barcodeImage);
        BufferedImage productImg = toBufferedImage(productImage);
        BarcodeLookupResponse response = barcodeLookupService.lookup(barcodeImg, productImg);

        return ResponseEntity.ok(response);
    }

    private BufferedImage toBufferedImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return ImageIO.read(file.getInputStream());
    }

    @GetMapping("/{ingredientId}")
    @Operation(
            summary = "재료 단건 조회",
            description = "재료의 세부 내역을 조회합니다."
    )
    public ResponseEntity<IngredientDetailResponse> readIngredientId(
            @PathVariable Long ingredientId,
            @AuthenticationPrincipal Long kakaoId
    ) {
        IngredientDetailResponse resp = ingredientService.readIngredient(ingredientId, kakaoId);
        return ResponseEntity.ok(resp);
    }


    @GetMapping
    @Operation(
            summary = "재료 목록 조회",
            description = "로그인한 유저 본인이 등록한 재료들을 조회합니다."
    )
    public ResponseEntity<List<IngredientListResponse>> readAllIngredients(@AuthenticationPrincipal Long kakaoId) {
        List<IngredientListResponse> list= ingredientService.readIngredientList(kakaoId);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{ingredientId}")
    @Operation(
            summary = "재료 수정",
            description = "재료 정보를 부분 수정합니다."
    )
    public ResponseEntity<IngredientDetailResponse> updateIngredient(
            @PathVariable Long ingredientId,
            @Valid @RequestBody IngredientUpdateRequest request,
            @AuthenticationPrincipal Long kakaoId
            ) {
        IngredientDetailResponse response = ingredientService.updateIngredient(ingredientId, request, kakaoId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ingredientId}")
    @Operation(
            summary = "재료 삭제",
            description = "재료를 삭제합니다."
    )
    public ResponseEntity<Void> deleteIngredient(
            @PathVariable Long ingredientId,
            @AuthenticationPrincipal Long kakaoId
    ) {
        ingredientService.deleteIngredient(ingredientId, kakaoId);
        return ResponseEntity.noContent().build();
    }
}
