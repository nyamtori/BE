package com.project.nyamtori.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {
    private final WebClient webClient;

    public GeminiClient(
            @Value("${gemini.url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey
    ){
        this.webClient=WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key",apiKey)
                .defaultHeader("Content-Type","application/json")
                .build();
    }

    public String generateRecipe(List<String> ingredients){

        String ingredientText = String.join(", ", ingredients);

        String prompt = """
                다음 재료만 사용해서 만들 수 있는 한국 요리 레시피 하나를 만들어줘.

                사용 가능한 재료:
                %s

                반드시 JSON 형식으로 응답해줘.

                {
                  "title": "요리 이름",
                  "ingredients": ["재료"],
                  "steps": ["조리과정1","조리과정2"]
                }
                """.formatted(ingredientText);

        Map<String,Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts",List.of(
                                        Map.of("text",prompt)
                                )
                        )
                )

        );

        return webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
