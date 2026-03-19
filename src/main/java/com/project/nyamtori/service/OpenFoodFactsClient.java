package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.OpenFoodFactsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OpenFoodFactsClient {
    private final RestClient restClient = RestClient.create();

    public ProductInfo lookup(String barcode) {
        String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";

        OpenFoodFactsResponse response = restClient.get()
                .uri(url)
                .retrieve()
                .body(OpenFoodFactsResponse.class);

        if (response == null || response.status() != 1 || response.product() == null) {
            throw new IllegalArgumentException("OpenFoodFacts에서도 제품 정보를 찾을 수 없습니다.");
        }

        return new ProductInfo(
                response.product().productName(),
                response.product().brands(),
                response.product().categories(),
                null
        );
    }
}
