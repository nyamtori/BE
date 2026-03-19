package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BarcodeLookupService {

    private final FoodSafetyClient foodSafetyClient;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final IngredientMatcher ingredientMatcher;
    private final StorageTypeResolver storageTypeResolver;

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
                productInfo.shelfLife(),
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