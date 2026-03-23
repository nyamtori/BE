package com.project.nyamtori.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenFoodFactsResponse(
        int status,
        Product product
) {

    public record Product(
            @JsonProperty("product_name")
            String productName,

            String brands,
            String categories
    ) {}
}