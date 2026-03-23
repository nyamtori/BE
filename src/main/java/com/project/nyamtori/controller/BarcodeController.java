package com.project.nyamtori.controller;

import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import com.project.nyamtori.service.BarcodeLookupService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/barcodes")
public class BarcodeController {

    private final BarcodeLookupService barcodeLookupService;

    @GetMapping("/{barcode}")
    @Operation(
            summary = "바코드 제품 조회",
            description = "식약처 바코드연계제품정보 API를 통해 제품 정보를 조회합니다."
    )
    public ResponseEntity<BarcodeLookupResponse> lookupBarcode(@PathVariable String barcode) {
        BarcodeLookupResponse response = barcodeLookupService.lookup(barcode);
        return ResponseEntity.ok(response);
    }
}