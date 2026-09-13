package com.project.nyamtori.dto.response;

public record BarcodeLookupResponse(
        String barcode,
        String productName,
        String manufacturerName,
        String foodType,
        String expirationDate,
        String ingredientName,
        String storageType
) {
}
