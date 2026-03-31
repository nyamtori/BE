package com.project.nyamtori.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RescaleOp;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class OcrService {

    private final Tesseract tesseract;
    private final String tessdataPath;
    private final String language;

    public OcrService(
            @Value("${ocr.tessdata-path}") String tessdataPath,
            @Value("${ocr.language}") String language
    ) {
        this.tessdataPath = tessdataPath;
        this.language = language;

        OpenCV.loadLocally(); // OpenCV 네이티브 라이브러리 로드

        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(Paths.get(tessdataPath).toAbsolutePath().normalize().toString());
        this.tesseract.setLanguage(language);

        // PSM 11: 방향/구조 무관하게 텍스트 찾기 — 도트 폰트에 가장 관대한 모드
        this.tesseract.setPageSegMode(11);
        this.tesseract.setOcrEngineMode(1); // LSTM 엔진
    }

    @PostConstruct
    public void checkOcrConfig() {
        Path path = Paths.get(tessdataPath).toAbsolutePath().normalize();
        log.info("OCR tessdataPath = {}", path);
        log.info("OCR language = {}", language);

        if (!Files.exists(path)) throw new IllegalArgumentException("tessdata 폴더가 없습니다: " + path);
        if (!Files.isDirectory(path)) throw new IllegalArgumentException("tessdata 경로가 폴더가 아닙니다: " + path);

        if (language.contains("eng")) {
            Path eng = path.resolve("eng.traineddata");
            log.info("eng exists = {}", Files.exists(eng));
            if (!Files.exists(eng)) throw new IllegalArgumentException("eng.traineddata 파일이 없습니다: " + eng);
        }
        if (language.contains("kor")) {
            Path kor = path.resolve("kor.traineddata");
            log.info("kor exists = {}", Files.exists(kor));
            if (!Files.exists(kor)) throw new IllegalArgumentException("kor.traineddata 파일이 없습니다: " + kor);
        }
    }

    public String extractExpirationDate(BufferedImage image) {
        if (image == null) throw new IllegalArgumentException("OCR 대상 이미지가 null입니다.");

        List<BufferedImage> candidates = getCandidateRegions(image);

        for (BufferedImage region : candidates) {
            try {
                BufferedImage preprocessed = preprocess(region);
                String rawText = tesseract.doOCR(preprocessed);
                log.info("OCR rawText = [{}]", rawText);

                String parsedDate = parseDate(rawText);
                if (parsedDate != null) {
                    log.info("OCR parsedDate = [{}]", parsedDate);
                    return parsedDate;
                }
            } catch (TesseractException e) {
                log.warn("OCR 실패 (영역 건너뜀): {}", e.getMessage());
            }
        }

        log.warn("모든 영역에서 날짜를 찾지 못했습니다.");
        return null;
    }

    /**
     * 후보 영역: 전체 → 상단 절반 → 하단 절반 → 좌하단 → 우하단
     * 전체 이미지를 가장 먼저 시도 (크롭으로 날짜를 잘라낼 위험 제거)
     */
    private List<BufferedImage> getCandidateRegions(BufferedImage image) {
        List<BufferedImage> regions = new ArrayList<>();
        int w = image.getWidth();
        int h = image.getHeight();

        regions.add(safeCopy(image, 0, 0, w, h));           // 전체
        regions.add(safeCopy(image, 0, h / 2, w, h / 2));   // 하단 절반
        regions.add(safeCopy(image, 0, 0, w, h / 2));        // 상단 절반
        regions.add(safeCopy(image, 0, h / 2, w / 2, h / 2)); // 좌하단
        regions.add(safeCopy(image, w / 2, h / 2, w / 2, h / 2)); // 우하단

        return regions;
    }

    /**
     * 전처리: 그레이스케일 → 3배 확대 → 팽창(dilation)으로 도트 연결 → 대비 강화
     * 도트 매트릭스 폰트는 점들이 떨어져 있어서 팽창으로 이어줘야 Tesseract가 글자로 인식
     */
    private BufferedImage preprocess(BufferedImage image) {
        // 1. BufferedImage → OpenCV Mat
        BufferedImage gray = toGray(image);
        Mat mat = bufferedImageToMat(gray);

        // 2. 3배 확대
        Mat enlarged = new Mat();
        Imgproc.resize(mat, enlarged, new Size(mat.cols() * 3, mat.rows() * 3), 0, 0, Imgproc.INTER_CUBIC);

        // 3. 이진화 (Otsu — 자동 임계값)
        Mat binary = new Mat();
        Imgproc.threshold(enlarged, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // 4. 팽창(dilation): 도트 사이 간격을 메워 글자처럼 연결
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Mat dilated = new Mat();
        Imgproc.dilate(binary, dilated, kernel, new org.opencv.core.Point(-1, -1), 1);

        // 5. 반전 복구 (Tesseract는 흰 배경 + 검은 글자를 선호)
        Mat result = new Mat();
        Core.bitwise_not(dilated, result);

        return matToBufferedImage(result);
    }

    private BufferedImage toGray(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    private Mat bufferedImageToMat(BufferedImage image) {
        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = converted.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        byte[] data = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int w = mat.cols(), h = mat.rows();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return image;
    }

    private BufferedImage safeCopy(BufferedImage src, int x, int y, int w, int h) {
        int maxW = src.getWidth();
        int maxH = src.getHeight();

        x = Math.max(0, Math.min(x, maxW - 1));
        y = Math.max(0, Math.min(y, maxH - 1));
        w = Math.min(w, maxW - x);
        h = Math.min(h, maxH - y);

        // 너무 작은 영역은 건너뜀
        if (w < 10 || h < 10) return src;

        BufferedImage sub = src.getSubimage(x, y, w, h);
        BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = copy.createGraphics();
        g.drawImage(sub, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * OCR 결과에서 날짜 파싱
     * 지원 포맷: 2026.04.07 / 2026-04-07 / 2026/04/07
     *           26.04.07 / 20260407 / 2026년04월07일 등
     */
    private String parseDate(String rawText) {
        if (rawText == null || rawText.isBlank()) return null;

        String normalized = rawText
                .replace("\n", " ")
                .replace("년", ".")
                .replace("월", ".")
                .replace("일", "")
                .replace(",", ".")
                .replace(";", ".")
                .replaceAll("[Ee][Xx][Pp][^0-9]*", "")
                .replaceAll("[Bb][Ee][Ss][Tt][^0-9]*", "")
                .replaceAll("유통기한", "")
                .replaceAll("소비기한", "")
                .replaceAll("\\s+", "")
                .trim();

        log.info("OCR normalized = [{}]", normalized);

        Pattern[] patterns = new Pattern[]{
                // 4자리 연도 + 구분자 (구분자가 뭉개져도 흡수)
                Pattern.compile("(20\\d{2})[^0-9]{1,3}(\\d{1,2})[^0-9]{1,3}(\\d{1,2})"),
                // 4자리 연도 + 구분자 없음
                Pattern.compile("(20\\d{2})(\\d{2})(\\d{2})"),
                // 2자리 연도
                Pattern.compile("(\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find()) {
                String y = matcher.group(1);
                String m = matcher.group(2);
                String d = matcher.group(3);

                if (y.length() == 2) y = "20" + y;

                int month = Integer.parseInt(m);
                int day   = Integer.parseInt(d);

                if (month < 1 || month > 12) continue;
                if (day   < 1 || day   > 31) continue;

                return String.format("%s-%02d-%02d", y, month, day);
            }
        }

        return null;
    }
}