package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import com.project.nyamtori.dto.response.FoodSafetyBarcodeApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class BarcodeLookupService {

    @Value("${foodsafety.api.key}")
    private String apiKey;

    @Value("${foodsafety.api.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public BarcodeLookupResponse lookup(String barcode) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .pathSegment(apiKey, "C005", "json", "1", "5", "BAR_CD=" + barcode)
                .toUriString();

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

        String ingredientName = extractIngredientName(item.productName(), item.foodType());
        String storageType = recommendStorageType(ingredientName);

        return new BarcodeLookupResponse(
                item.barCd(),
                item.productName(),
                item.manufacturerName(),
                item.foodType(),
                item.shelfLife(),
                ingredientName,
                storageType
        );
    }

    private String extractIngredientName(String productName, String foodType) {
        String source = safeLower(productName) + " " + safeLower(foodType);

        if (containsAny(source, "우유", "milk")) return "우유";
        if (containsAny(source, "계란", "달걀", "특란", "대란", "란")) return "계란";
        if (containsAny(source, "두부")) return "두부";
        if (containsAny(source, "된장")) return "된장";
        if (containsAny(source, "고추장")) return "고추장";
        if (containsAny(source, "간장")) return "간장";
        if (containsAny(source, "참기름")) return "참기름";
        if (containsAny(source, "들기름")) return "들기름";
        if (containsAny(source, "식용유", "콩기름", "포도씨유", "올리브유")) return "식용유";
        if (containsAny(source, "양파")) return "양파";
        if (containsAny(source, "대파", "쪽파", "실파")) return "파";
        if (containsAny(source, "마늘")) return "마늘";
        if (containsAny(source, "감자")) return "감자";
        if (containsAny(source, "고구마")) return "고구마";
        if (containsAny(source, "당근")) return "당근";
        if (containsAny(source, "오이")) return "오이";
        if (containsAny(source, "애호박", "호박")) return "호박";
        if (containsAny(source, "배추")) return "배추";
        if (containsAny(source, "양배추")) return "양배추";
        if (containsAny(source, "상추")) return "상추";
        if (containsAny(source, "깻잎")) return "깻잎";
        if (containsAny(source, "버섯", "새송이", "팽이", "느타리", "표고")) return "버섯";
        if (containsAny(source, "사과")) return "사과";
        if (containsAny(source, "바나나")) return "바나나";
        if (containsAny(source, "딸기")) return "딸기";
        if (containsAny(source, "토마토")) return "토마토";
        if (containsAny(source, "쌀")) return "쌀";
        if (containsAny(source, "라면")) return "라면";
        if (containsAny(source, "만두")) return "만두";
        if (containsAny(source, "김치")) return "김치";
        if (containsAny(source, "치즈")) return "치즈";
        if (containsAny(source, "요거트", "요구르트", "요구르트", "yogurt")) return "요거트";
        if (containsAny(source, "햄", "소시지", "비엔나")) return "햄";
        if (containsAny(source, "베이컨")) return "베이컨";
        if (containsAny(source, "닭", "닭가슴살", "치킨")) return "닭고기";
        if (containsAny(source, "돼지", "삼겹", "목살", "돼지고기")) return "돼지고기";
        if (containsAny(source, "소고기", "한우", "우육")) return "소고기";
        if (containsAny(source, "참치")) return "참치";
        if (containsAny(source, "고등어")) return "고등어";
        if (containsAny(source, "오징어")) return "오징어";
        if (containsAny(source, "새우")) return "새우";
        if (containsAny(source, "국수", "소면", "중면", "우동면")) return "면";
        if (containsAny(source, "밀가루", "부침가루", "튀김가루")) return "가루";
        if (containsAny(source, "설탕")) return "설탕";
        if (containsAny(source, "소금")) return "소금";

        if (foodType != null && !foodType.isBlank()) {
            return foodType;
        }
        return productName;
    }

    private String recommendStorageType(String ingredientName) {
        String source = safeLower(ingredientName);

        if (containsAny(source, "우유", "계란", "두부", "치즈", "요거트", "햄", "베이컨", "김치")) {
            return "냉장";
        }

        if (containsAny(source, "만두", "냉동", "새우", "오징어", "고등어")) {
            return "냉동";
        }

        return "상온";
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}