package com.project.nyamtori.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient geminiWebClient;

    private final ObjectMapper objectMapper;

    public String generateRecipe(List<String> ingredients){

        String ingredientText = String.join(", ", ingredients);

        String prompt = """
                다음 재료만 사용해서 만들 수 있는 요리 레시피 3개를 만들어줘.
                
                각 레시피는 서로 다른 요리여야 한다.

                사용 가능한 재료:
                %s
                
                각 재료의 양은 반드시 **1인분 기준**으로 작성해라.

                반드시 JSON 형식으로 응답해줘.

                {
                   "recipes":[
                     {
                       "title":"요리 이름",
                       "ingredients":[
                            {
                                "name":"재료",
                                "amount":"양"
                            }
                        ],
                       "steps":["조리과정1","조리과정2"]
                     }
                   ]
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

        String response = geminiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try{
            JsonNode root = objectMapper.readTree(response);


            String text = root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();


            text = text.replace("```json", "")
                    .replace("```", "")
                    .trim();

            return text;

        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패");
        }
    }

}
