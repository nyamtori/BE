package com.project.nyamtori.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FoodSafetyBarcodeApiResponse(
        @JsonProperty("C005")
        C005 c005
) {
    public record C005(
            @JsonProperty("total_count")
            String totalCount,

            @JsonProperty("RESULT")
            Result result,

            @JsonProperty("row")
            List<Row> row
    ) {
    }

    public record Result(
            @JsonProperty("MSG")
            String msg,

            @JsonProperty("CODE")
            String code
    ) {
    }

    public record Row(
            @JsonProperty("BAR_CD")
            String barCd,

            @JsonProperty("PRDLST_NM")
            String productName,

            @JsonProperty("BSSH_NM")
            String manufacturerName,

            @JsonProperty("PRDLST_DCNM")
            String foodType,

            @JsonProperty("POG_DAYCNT")
            String shelfLife
    ) {
    }
}