# 바코드 사진 + 제품 사진 2장으로 재료 등록 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 바코드 사진과 제품 사진 2장을 받아, 서버가 바코드를 직접 디코딩하고(ZXing), 기존 제품 조회/재료명 추출/보관위치 추천/유통기한 OCR 로직을 재사용해 `BarcodeLookupResponse`를 반환하도록 `/api/v1/ingredients/lookup`을 바꾸고, 보관위치 값을 냉장/냉동/실온 3가지로 백엔드에서 검증한다.

**Architecture:** 신규 `BarcodeDecoder`(ZXing 기반)를 `BarcodeLookupService`에 주입해 바코드 이미지를 디코딩한 뒤, 기존 `FoodSafetyClient → OpenFoodFactsClient(fallback) → IngredientMatcher → StorageTypeResolver → OcrService` 파이프라인을 그대로 태운다. `IngredientController`는 `barcode`(문자열) 파라미터를 없애고 `barcodeImage` 파트를 추가로 받는다. 보관위치는 커스텀 Bean Validation 애노테이션(`@ValidStorageLocation`)으로 제한한다.

**Tech Stack:** Spring Boot, ZXing(`com.google.zxing:core`, `com.google.zxing:javase`) 3.5.4, JUnit5, Mockito, AssertJ, Jakarta Bean Validation(Hibernate Validator)

**Branch:** `feat/yebin/23` (이미 `develop` 최신 상태로 rebase됨, 별도 체크아웃 불필요)

---

## Task 1: ZXing 의존성 추가

**Files:**
- Modify: `build.gradle`

- [ ] **Step 1: build.gradle에 ZXing 의존성 추가**

`build.gradle`의 `dependencies { ... }` 블록에서 `// OCR` 주석 위에 아래를 추가:

```groovy
	// Barcode
	implementation 'com.google.zxing:core:3.5.4'
	implementation 'com.google.zxing:javase:3.5.4'

```

- [ ] **Step 2: 의존성 다운로드 확인**

Run: `./gradlew -q dependencies --configuration compileClasspath | grep zxing`
Expected:
```
+--- com.google.zxing:core:3.5.4
+--- com.google.zxing:javase:3.5.4
```

- [ ] **Step 3: Commit**

```bash
git add build.gradle
git commit -m "build: ZXing 바코드 디코딩 의존성 추가"
```

---

## Task 2: BarcodeDecoder (바코드 사진 → 바코드 값)

**Files:**
- Create: `src/main/java/com/project/nyamtori/service/BarcodeDecoder.java`
- Test: `src/test/java/com/project/nyamtori/service/BarcodeDecoderTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
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
```

- [ ] **Step 2: 테스트 실행 → 컴파일 실패(RED) 확인**

Run: `./gradlew -q test --tests "com.project.nyamtori.service.BarcodeDecoderTest"`
Expected: FAIL — `cannot find symbol: class BarcodeDecoder` (compileTestJava 실패)

- [ ] **Step 3: 최소 구현 작성**

```java
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
```

- [ ] **Step 4: 테스트 실행 → 통과(GREEN) 확인**

Run: `./gradlew -q test --tests "com.project.nyamtori.service.BarcodeDecoderTest"`
Expected: PASS (2 tests, 0 failures)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/project/nyamtori/service/BarcodeDecoder.java src/test/java/com/project/nyamtori/service/BarcodeDecoderTest.java
git commit -m "feat: ZXing 기반 바코드 이미지 디코더 추가"
```

---

## Task 3: 보관위치(location) 3가지 값 검증

**Files:**
- Create: `src/main/java/com/project/nyamtori/validation/ValidStorageLocation.java`
- Create: `src/main/java/com/project/nyamtori/validation/StorageLocationValidator.java`
- Test: `src/test/java/com/project/nyamtori/validation/StorageLocationValidatorTest.java`
- Modify: `src/main/java/com/project/nyamtori/dto/request/IngredientCreateRequest.java`
- Modify: `src/main/java/com/project/nyamtori/dto/request/IngredientUpdateRequest.java`
- Modify: `src/main/java/com/project/nyamtori/controller/IngredientController.java:89-91`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
package com.project.nyamtori.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StorageLocationValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    static class Sample {
        @ValidStorageLocation
        String location;

        Sample(String location) {
            this.location = location;
        }
    }

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void allowsThreeValidLocations() {
        assertThat(validator.validate(new Sample("냉장"))).isEmpty();
        assertThat(validator.validate(new Sample("냉동"))).isEmpty();
        assertThat(validator.validate(new Sample("실온"))).isEmpty();
    }

    @Test
    void rejectsInvalidLocation() {
        Set<ConstraintViolation<Sample>> violations = validator.validate(new Sample("냉장고"));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void allowsNull_soOptionalUpdateFieldIsUnaffected() {
        assertThat(validator.validate(new Sample(null))).isEmpty();
    }
}
```

- [ ] **Step 2: 테스트 실행 → 컴파일 실패(RED) 확인**

Run: `./gradlew -q test --tests "com.project.nyamtori.validation.StorageLocationValidatorTest"`
Expected: FAIL — `cannot find symbol: class ValidStorageLocation`

- [ ] **Step 3: 애노테이션 + validator 구현**

```java
package com.project.nyamtori.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StorageLocationValidator.class)
public @interface ValidStorageLocation {
    String message() default "보관위치는 냉장, 냉동, 실온 중 하나여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
package com.project.nyamtori.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class StorageLocationValidator implements ConstraintValidator<ValidStorageLocation, String> {

    private static final Set<String> ALLOWED_LOCATIONS = Set.of("냉장", "냉동", "실온");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // 필수 여부는 @NotBlank 등 별도 애노테이션이 담당
        }
        return ALLOWED_LOCATIONS.contains(value);
    }
}
```

- [ ] **Step 4: 테스트 실행 → 통과(GREEN) 확인**

Run: `./gradlew -q test --tests "com.project.nyamtori.validation.StorageLocationValidatorTest"`
Expected: PASS (3 tests, 0 failures)

- [ ] **Step 5: DTO에 애노테이션 적용**

`IngredientCreateRequest.java`의 `location` 필드를 수정:

```java
    @NotBlank(message = "보관 방법을 선택해주세요.")
    @ValidStorageLocation
    private String location;
```

(import 추가: `import com.project.nyamtori.validation.ValidStorageLocation;`)

`IngredientUpdateRequest.java`의 `location` 필드를 수정:

```java
    @ValidStorageLocation
    private String location;
```

(import 추가: `import com.project.nyamtori.validation.ValidStorageLocation;`)

- [ ] **Step 6: IngredientController의 update 메서드에 `@Valid` 추가**

`src/main/java/com/project/nyamtori/controller/IngredientController.java`의 `updateIngredient` 메서드(89-91번째 줄 부근)를 수정:

```java
    public ResponseEntity<IngredientDetailResponse> updateIngredient(
            @PathVariable Long ingredientId,
            @Valid @RequestBody IngredientUpdateRequest request
            ) {
```

(`@Valid`는 이미 파일 상단에 `import jakarta.validation.Valid;`로 import되어 있음 — 없으면 추가)

- [ ] **Step 7: 전체 컴파일 확인**

Run: `./gradlew -q compileJava compileTestJava`
Expected: BUILD SUCCESSFUL (에러 없음)

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/project/nyamtori/validation/ src/test/java/com/project/nyamtori/validation/ src/main/java/com/project/nyamtori/dto/request/IngredientCreateRequest.java src/main/java/com/project/nyamtori/dto/request/IngredientUpdateRequest.java src/main/java/com/project/nyamtori/controller/IngredientController.java
git commit -m "feat: 보관위치를 냉장/냉동/실온 3가지로 제한하는 검증 추가"
```

---

## Task 4: BarcodeLookupService — 바코드 사진 + 제품 사진으로 조회

**Files:**
- Modify: `src/main/java/com/project/nyamtori/service/BarcodeLookupService.java`
- Test: `src/test/java/com/project/nyamtori/service/BarcodeLookupServiceTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarcodeLookupServiceTest {

    @Mock private FoodSafetyClient foodSafetyClient;
    @Mock private OpenFoodFactsClient openFoodFactsClient;
    @Mock private IngredientMatcher ingredientMatcher;
    @Mock private StorageTypeResolver storageTypeResolver;
    @Mock private OcrService ocrService;
    @Mock private BarcodeDecoder barcodeDecoder;

    @InjectMocks
    private BarcodeLookupService barcodeLookupService;

    private final BufferedImage barcodeImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage productImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    @Test
    void lookup_decodesBarcodeImage_andAssemblesResponse() {
        when(barcodeDecoder.decode(barcodeImage)).thenReturn("8801062626625");
        ProductInfo productInfo = new ProductInfo("서울우유", "서울우유협동조합", "우유류", null);
        when(foodSafetyClient.lookup("8801062626625")).thenReturn(productInfo);
        when(ingredientMatcher.extract("서울우유", "우유류")).thenReturn("우유");
        when(storageTypeResolver.resolve("우유")).thenReturn("냉장");
        when(ocrService.extractExpirationDate(productImage)).thenReturn("2026-08-01");

        BarcodeLookupResponse response = barcodeLookupService.lookup(barcodeImage, productImage);

        assertThat(response).isEqualTo(new BarcodeLookupResponse(
                "8801062626625", "서울우유", "서울우유협동조합", "우유류", "2026-08-01", "우유", "냉장"
        ));
        verifyNoInteractions(openFoodFactsClient);
    }

    @Test
    void lookup_fallsBackToOpenFoodFacts_whenFoodSafetyFails() {
        when(barcodeDecoder.decode(barcodeImage)).thenReturn("8801062626625");
        when(foodSafetyClient.lookup("8801062626625")).thenThrow(new IllegalArgumentException("조회 실패"));
        ProductInfo fallback = new ProductInfo("Milk", "Brand", "Dairy", null);
        when(openFoodFactsClient.lookup("8801062626625")).thenReturn(fallback);
        when(ingredientMatcher.extract("Milk", "Dairy")).thenReturn("우유");
        when(storageTypeResolver.resolve("우유")).thenReturn("냉장");
        when(ocrService.extractExpirationDate(productImage)).thenReturn("2026-08-01");

        BarcodeLookupResponse response = barcodeLookupService.lookup(barcodeImage, productImage);

        assertThat(response.productName()).isEqualTo("Milk");
    }

    @Test
    void lookup_throws_whenExpirationDateNotFound() {
        when(barcodeDecoder.decode(barcodeImage)).thenReturn("8801062626625");
        ProductInfo productInfo = new ProductInfo("서울우유", "서울우유협동조합", "우유류", null);
        when(foodSafetyClient.lookup("8801062626625")).thenReturn(productInfo);
        when(ingredientMatcher.extract("서울우유", "우유류")).thenReturn("우유");
        when(storageTypeResolver.resolve("우유")).thenReturn("냉장");
        when(ocrService.extractExpirationDate(productImage)).thenReturn(null);

        assertThatThrownBy(() -> barcodeLookupService.lookup(barcodeImage, productImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유통기한을 인식하지 못했습니다");
    }
}
```

- [ ] **Step 2: 테스트 실행 → 컴파일 실패(RED) 확인**

Run: `./gradlew -q test --tests "com.project.nyamtori.service.BarcodeLookupServiceTest"`
Expected: FAIL — `cannot find symbol: method lookup(BufferedImage,BufferedImage)`

- [ ] **Step 3: BarcodeLookupService 수정**

`src/main/java/com/project/nyamtori/service/BarcodeLookupService.java` 전체를 아래로 교체:

```java
package com.project.nyamtori.service;

import com.project.nyamtori.dto.response.BarcodeLookupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
@Slf4j
@RequiredArgsConstructor
public class BarcodeLookupService {

    private final FoodSafetyClient foodSafetyClient;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final IngredientMatcher ingredientMatcher;
    private final StorageTypeResolver storageTypeResolver;
    private final OcrService ocrService;
    private final BarcodeDecoder barcodeDecoder;

    // 바코드 값 문자열만으로 조회 (BarcodeController에서 사용)
    public BarcodeLookupResponse lookup(String barcode) {
        ProductInfo productInfo = findProductInfo(barcode);

        String ingredientName = ingredientMatcher.extract(
                productInfo.productName(),
                productInfo.foodType()
        );

        String storageType = storageTypeResolver.resolve(ingredientName);

        return new BarcodeLookupResponse(
                barcode,
                productInfo.productName(),
                productInfo.manufacturerName(),
                productInfo.foodType(),
                null,
                ingredientName,
                storageType
        );
    }

    // 바코드 사진 + 제품 사진(유통기한 OCR)
    public BarcodeLookupResponse lookup(BufferedImage barcodeImage, BufferedImage productImage) {
        String barcode = barcodeDecoder.decode(barcodeImage);
        return lookupWithExpirationImage(barcode, productImage);
    }

    private BarcodeLookupResponse lookupWithExpirationImage(String barcode, BufferedImage expirationImage) {
        ProductInfo productInfo = findProductInfo(barcode);

        String ingredientName = ingredientMatcher.extract(
                productInfo.productName(),
                productInfo.foodType()
        );

        String storageType = storageTypeResolver.resolve(ingredientName);

        String expirationDate = ocrService.extractExpirationDate(expirationImage);

        if (expirationDate == null || expirationDate.isBlank()) {
            throw new IllegalArgumentException("유통기한을 인식하지 못했습니다. 유통기한 부분만 다시 가까이 촬영해주세요.");
        }

        return new BarcodeLookupResponse(
                barcode,
                productInfo.productName(),
                productInfo.manufacturerName(),
                productInfo.foodType(),
                expirationDate,
                ingredientName,
                storageType
        );
    }

    private ProductInfo findProductInfo(String barcode) {
        try {
            return foodSafetyClient.lookup(barcode);
        } catch (Exception e) {
            return openFoodFactsClient.lookup(barcode);
        }
    }
}
```

- [ ] **Step 4: 테스트 실행 → 통과(GREEN) 확인**

Run: `./gradlew -q test --tests "com.project.nyamtori.service.BarcodeLookupServiceTest"`
Expected: PASS (3 tests, 0 failures)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/project/nyamtori/service/BarcodeLookupService.java src/test/java/com/project/nyamtori/service/BarcodeLookupServiceTest.java
git commit -m "feat: 바코드 사진+제품 사진으로 재료 정보 조회하도록 BarcodeLookupService 변경"
```

---

## Task 5: IngredientController — 엔드포인트를 이미지 2장으로 변경

**Files:**
- Modify: `src/main/java/com/project/nyamtori/controller/IngredientController.java:48-61`

- [ ] **Step 1: lookupIngredient 메서드 교체**

`IngredientController.java`의 기존 메서드:

```java
    @PostMapping(value = "/lookup", consumes = "multipart/form-data")
    @Operation(
            summary = "바코드 조회 및 유통기한 추출",
            description = "바코드를 인식하고, 유통기한을 이미지에서 추출합니다."
    )
    public ResponseEntity<BarcodeLookupResponse> lookupIngredient(
            @RequestParam("barcode") String barcode,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        BufferedImage img = ImageIO.read(image.getInputStream());
        BarcodeLookupResponse response = barcodeLookupService.lookup(barcode, img);

        return ResponseEntity.ok(response);
    }
```

를 아래로 교체:

```java
    @PostMapping(value = "/lookup", consumes = "multipart/form-data")
    @Operation(
            summary = "바코드 사진 + 제품 사진으로 조회",
            description = "바코드 사진에서 바코드를 인식하고, 제품 사진에서 유통기한을 추출합니다."
    )
    public ResponseEntity<BarcodeLookupResponse> lookupIngredient(
            @RequestPart("barcodeImage") MultipartFile barcodeImage,
            @RequestPart("productImage") MultipartFile productImage
    ) throws IOException {
        BufferedImage barcodeImg = ImageIO.read(barcodeImage.getInputStream());
        BufferedImage productImg = ImageIO.read(productImage.getInputStream());
        BarcodeLookupResponse response = barcodeLookupService.lookup(barcodeImg, productImg);

        return ResponseEntity.ok(response);
    }
```

- [ ] **Step 2: 전체 컴파일 확인**

Run: `./gradlew -q compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/project/nyamtori/controller/IngredientController.java
git commit -m "feat: /api/v1/ingredients/lookup이 바코드 사진+제품 사진 2장을 받도록 변경"
```

---

## Task 6: 전체 검증

**Files:** (없음 — 실행만)

- [ ] **Step 1: 전체 테스트 실행**

Run: `./gradlew -q test`
Expected: `NyamtoriApplicationTests#contextLoads`를 제외한 모든 테스트 PASS. (`contextLoads`는 로컬에 `JWT_SECRET`/`KAKAO_REST_API_KEY`/DB가 없어서 이 브랜치 이전부터 실패하던 것이므로 이번 변경과 무관 — 새로 실패하는 다른 테스트가 없는지만 확인)

- [ ] **Step 2: 새로 생긴 테스트 결과 확인**

Run: `find build/test-results -name "*.xml" | xargs grep -l 'failures="[1-9]"'`
Expected: `NyamtoriApplicationTests.xml`만 나와야 함 (다른 실패 없음)
