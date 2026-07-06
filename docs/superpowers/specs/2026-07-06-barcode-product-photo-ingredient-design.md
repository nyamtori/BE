# 바코드 사진 + 제품 사진 2장으로 재료 등록 (feat/yebin/23)

## 배경
기존 `/api/v1/ingredients/lookup`은 바코드 값(문자열, 클라이언트가 미리 스캔/디코딩)과 제품 사진(유통기한 OCR용) 1장을 받아 제품 정보를 조회하고 유통기한을 추출했다. 요구사항 변경으로, 바코드도 사진으로 촬영해 서버가 직접 디코딩하도록 바꾼다. 최종 목표는 사용자가 바코드 사진 + 제품 사진 2장만 찍으면 유통기한, 재료 이름, 보관위치(냉장/냉동/실온 중 선택)가 채워진 상태로 재료 등록 화면을 받는 것이다.

## 범위
- 바코드 사진에서 서버가 바코드 값을 직접 디코딩 (ZXing 사용)
- 기존 제품 조회(FoodSafety → OpenFoodFacts fallback), 재료명 추출(IngredientMatcher), 보관위치 추천(StorageTypeResolver), 유통기한 OCR(OcrService) 로직은 그대로 재사용
- 보관위치(location)를 백엔드에서 "냉장"/"냉동"/"실온" 3가지 값으로만 검증하도록 강화
- 최종 저장(`POST /api/v1/ingredients/manual`)은 변경 없음 — 사용자가 lookup 결과를 확인/수정 후 그대로 호출

## 비용 검토
이 기능에 쓰이는 모든 외부 라이브러리/API는 무료다.
- **ZXing** (신규): Apache License 2.0, 로컬 라이브러리, API 키/호출과금 없음
- **Tesseract(tess4j) / OpenCV**: 기존 사용 중, 오픈소스, 로컬 실행
- **식품안전나라 Open API**: 공공데이터포털 활용신청으로 발급받은 무료 API. 일일 호출 트래픽 제한은 있으나 과금 없음
- **Open Food Facts API**: 무료, API 키 불필요, 분당 15회/IP 제한

## API 변경
### `POST /api/v1/ingredients/lookup` (multipart/form-data)
**변경 전**
```
barcode: String (RequestParam)
image: MultipartFile (RequestPart, 제품/유통기한 사진)
```
**변경 후**
```
barcodeImage: MultipartFile (RequestPart, 바코드 사진)
productImage: MultipartFile (RequestPart, 제품/유통기한 사진)
```

응답(`BarcodeLookupResponse`)은 그대로: `barcode, productName, manufacturerName, foodType, expirationDate, ingredientName, storageType`

## 컴포넌트

### `BarcodeDecoder` (신규)
```java
String decode(BufferedImage barcodeImage)
```
- ZXing `MultiFormatReader`로 바코드(EAN-13 등) 디코딩
- 실패 시 `IllegalArgumentException("바코드를 인식하지 못했습니다. 바코드 부분만 다시 가까이 촬영해주세요.")`

### `BarcodeLookupService` (수정)
- 기존 `lookup(String barcode)` / `lookup(String barcode, BufferedImage expirationImage)` 오버로드 대신, `lookup(BufferedImage barcodeImage, BufferedImage productImage)` 단일 진입점으로 통일
- 내부 흐름: `BarcodeDecoder.decode(barcodeImage)` → `findProductInfo(barcode)` → `IngredientMatcher.extract(...)` → `StorageTypeResolver.resolve(...)` → `OcrService.extractExpirationDate(productImage)` → `BarcodeLookupResponse` 조립
- 기존 `lookup(String barcode)` (바코드 값만으로 조회, 사진 없음) 오버로드는 `BarcodeController.lookupBarcode()`(`GET /api/v1/barcodes/{barcode}`)가 그대로 사용하므로 삭제하지 않고 유지한다. 삭제 대상은 `lookup(String barcode, BufferedImage expirationImage)` 오버로드뿐이며, 이를 `lookup(BufferedImage barcodeImage, BufferedImage productImage)`로 교체한다.

### `IngredientController` (수정)
```java
@PostMapping(value = "/lookup", consumes = "multipart/form-data")
public ResponseEntity<BarcodeLookupResponse> lookupIngredient(
        @RequestPart("barcodeImage") MultipartFile barcodeImage,
        @RequestPart("productImage") MultipartFile productImage
) throws IOException
```

### 보관위치 검증 (수정)
- `IngredientCreateRequest.location`, `IngredientUpdateRequest.location`에 `냉장`/`냉동`/`실온`만 허용하는 검증 추가 (커스텀 애노테이션 또는 `@Pattern(regexp = "냉장|냉동|실온")`)
- 검증 실패 시 기존 `@Valid` 흐름을 타므로 400 + 명확한 메시지 반환

## 에러 처리
- 바코드 사진 디코딩 실패 → 재촬영 안내 메시지 (위 `BarcodeDecoder` 예외)
- 바코드는 읽혔지만 FoodSafety/OpenFoodFacts 둘 다 조회 실패 → 기존 예외 전파 유지
- 유통기한 OCR 실패 → 기존 메시지 유지 ("유통기한을 인식하지 못했습니다...")
- `location` 값이 3가지 외 → validation 에러

## 테스트 계획
- `BarcodeDecoderTest`: 샘플 바코드 이미지 디코딩 성공 / 인식 불가 이미지 실패 케이스
- `BarcodeLookupServiceTest`: Mock(`BarcodeDecoder`, `FoodSafetyClient`, `OpenFoodFactsClient`, `IngredientMatcher`, `StorageTypeResolver`, `OcrService`)으로 전체 조합 로직 검증, FoodSafety 실패 시 OpenFoodFacts fallback 검증
- `IngredientCreateRequest`/`UpdateRequest` location validator 단위 테스트 (허용값 3개 통과, 그 외 값 실패)

## 비고
- 프론트엔드에서 보관위치 드롭다운(냉장/냉동/실온)을 `storageType` 추천값으로 미리 채워주고, 사용자가 확정/변경 후 `/manual`로 저장하는 흐름은 이번 변경 범위 밖(이미 프론트에서 처리하거나 별도 작업).
