package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
@Slf4j
@RequiredArgsConstructor
public class BarcodeLookupService {

    private final FoodSafetyClient foodSafetyClient;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final IngredientMatcher ingredientMatcher;
    private final StorageTypeResolver storageTypeResolver;
    private final OcrService ocrService;

    // 바코드만 조회
    public BarcodeLookupResponse lookup(String barcode) {
        ProductInfo productInfo = findProductInfo(barcode);

        String ingredientName = ingredientMatcher.extract(
                productInfo.productName(),
                productInfo.foodType()
        );

        String storageType = storageTypeResolver.resolve(ingredientName);

        return new BarcodeLookupResponse(
                barcode,
                productInfo.productName(),
                productInfo.manufacturerName(),
                productInfo.foodType(),
                null,
                ingredientName,
                storageType
        );
    }

    // 바코드 + 이미지 OCR
    public BarcodeLookupResponse lookup(String barcode, BufferedImage expirationImage) {
        ProductInfo productInfo = findProductInfo(barcode);

        String ingredientName = ingredientMatcher.extract(
                productInfo.productName(),
                productInfo.foodType()
        );

        String storageType = storageTypeResolver.resolve(ingredientName);

        String expirationDate = ocrService.extractExpirationDate(expirationImage);

        if (expirationDate == null || expirationDate.isBlank()) {
            throw new IllegalArgumentException("유통기한을 인식하지 못했습니다. 유통기한 부분만 다시 가까이 촬영해주세요.");
        }

        return new BarcodeLookupResponse(
                barcode,
                productInfo.productName(),
                productInfo.manufacturerName(),
                productInfo.foodType(),
                expirationDate,
                ingredientName,
                storageType
        );
    }

    private ProductInfo findProductInfo(String barcode) {
        try {
            return foodSafetyClient.lookup(barcode);
        } catch (Exception e) {
            return openFoodFactsClient.lookup(barcode);
        }
    }
}