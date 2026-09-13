# --- Build stage ---
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-jammy

# tess4j talks to the tesseract/leptonica shared libs via JNA, so they must be
# installed natively here — the traineddata models themselves ship in ./tessdata.
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr libtesseract-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
COPY tessdata tessdata

ENV TESSDATA_PATH=/app/tessdata
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75 -jar app.jar"]
