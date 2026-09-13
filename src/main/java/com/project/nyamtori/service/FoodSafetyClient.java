package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.FoodSafetyBarcodeApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class FoodSafetyClient {

    @Value("${foodsafety.api.key}")
    private String apiKey;

    @Value("${foodsafety.api.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public ProductInfo lookup(String barcode) {
        String url = baseUrl + "/" + apiKey + "/C005/json/1/5/BAR_CD=" + barcode;

        FoodSafetyBarcodeApiResponse response = restClient.get()
                .uri(url)
                .retrieve()
                .body(FoodSafetyBarcodeApiResponse.class);

        if (response == null || response.c005() == null) {
            throw new IllegalArgumentException("식약처 응답이 비어 있습니다.");
        }

        FoodSafetyBarcodeApiResponse.C005 c005 = response.c005();

        if ("0".equals(c005.totalCount()) || c005.row() == null || c005.row().isEmpty()) {
            throw new IllegalArgumentException("해당 바코드의 제품 정보를 찾을 수 없습니다.");
        }

        FoodSafetyBarcodeApiResponse.Row item = c005.row().get(0);

        return new ProductInfo(
                item.productName(),
                item.manufacturerName(),
                item.foodType(),
                item.shelfLife()
        );
    }
}