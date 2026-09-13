package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarcodeLookupServiceTest {

    @Mock private FoodSafetyClient foodSafetyClient;
    @Mock private OpenFoodFactsClient openFoodFactsClient;
    @Mock private IngredientMatcher ingredientMatcher;
    @Mock private StorageTypeResolver storageTypeResolver;
    @Mock private OcrService ocrService;
    @Mock private BarcodeDecoder barcodeDecoder;

    @InjectMocks
    private BarcodeLookupService barcodeLookupService;

    private final BufferedImage barcodeImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage productImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    @Test
    void lookup_decodesBarcodeImage_andAssemblesResponse() {
        when(barcodeDecoder.decode(barcodeImage)).thenReturn("8801062626625");
        ProductInfo productInfo = new ProductInfo("서울우유", "서울우유협동조합", "우유류", null);
        when(foodSafetyClient.lookup("8801062626625")).thenReturn(productInfo);
        when(ingredientMatcher.extract("서울우유", "우유류")).thenReturn("우유");
        when(storageTypeResolver.resolve("우유")).thenReturn("냉장");
        when(ocrService.extractExpirationDate(productImage)).thenReturn("2026-08-01");

        BarcodeLookupResponse response = barcodeLookupService.lookup(barcodeImage, productImage);

        assertThat(response).isEqualTo(new BarcodeLookupResponse(
                "8801062626625", "서울우유", "서울우유협동조합", "우유류", "2026-08-01", "우유", "냉장"
        ));
        verifyNoInteractions(openFoodFactsClient);
    }

    @Test
    void lookup_fallsBackToOpenFoodFacts_whenFoodSafetyFails() {
        when(barcodeDecoder.decode(barcodeImage)).thenReturn("8801062626625");
        when(foodSafetyClient.lookup("8801062626625")).thenThrow(new IllegalArgumentException("조회 실패"));
        ProductInfo fallback = new ProductInfo("Milk", "Brand", "Dairy", null);
        when(openFoodFactsClient.lookup("8801062626625")).thenReturn(fallback);
        when(ingredientMatcher.extract("Milk", "Dairy")).thenReturn("우유");
        when(storageTypeResolver.resolve("우유")).thenReturn("냉장");
        when(ocrService.extractExpirationDate(productImage)).thenReturn("2026-08-01");

        BarcodeLookupResponse response = barcodeLookupService.lookup(barcodeImage, productImage);

        assertThat(response.productName()).isEqualTo("Milk");
    }

    @Test
    void lookup_throws_whenExpirationDateNotFound() {
        when(barcodeDecoder.decode(barcodeImage)).thenReturn("8801062626625");
        ProductInfo productInfo = new ProductInfo("서울우유", "서울우유협동조합", "우유류", null);
        when(foodSafetyClient.lookup("8801062626625")).thenReturn(productInfo);
        when(ingredientMatcher.extract("서울우유", "우유류")).thenReturn("우유");
        when(storageTypeResolver.resolve("우유")).thenReturn("냉장");
        when(ocrService.extractExpirationDate(productImage)).thenReturn(null);

        assertThatThrownBy(() -> barcodeLookupService.lookup(barcodeImage, productImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유통기한을 인식하지 못했습니다");
    }
}
