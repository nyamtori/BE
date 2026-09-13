package com.project.nyamtori.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarcodeDecoderTest {

    private final BarcodeDecoder barcodeDecoder = new BarcodeDecoder();

    @Test
    void decode_returnsOriginalBarcode_fromGeneratedImage() throws Exception {
        String barcode = "8801062626625"; // 유효한 체크섬을 가진 EAN-13
        BitMatrix matrix = new EAN13Writer().encode(barcode, BarcodeFormat.EAN_13, 300, 150);
        BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(matrix);

        String result = barcodeDecoder.decode(barcodeImage);

        assertThat(result).isEqualTo(barcode);
    }

    @Test
    void decode_throws_whenImageHasNoBarcode() {
        BufferedImage blank = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        assertThatThrownBy(() -> barcodeDecoder.decode(blank))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("바코드를 인식하지 못했습니다");
    }
}
