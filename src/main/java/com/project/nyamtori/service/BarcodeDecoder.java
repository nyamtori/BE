package com.project.nyamtori.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
@Slf4j
public class BarcodeDecoder {

    private final MultiFormatReader reader = new MultiFormatReader();

    public String decode(BufferedImage barcodeImage) {
        if (barcodeImage == null) {
            throw new IllegalArgumentException("바코드 이미지가 null입니다.");
        }

        LuminanceSource source = new BufferedImageLuminanceSource(barcodeImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            log.warn("바코드 디코딩 실패: {}", e.getMessage());
            throw new IllegalArgumentException("바코드를 인식하지 못했습니다. 바코드 부분만 다시 가까이 촬영해주세요.");
        }
    }
}
