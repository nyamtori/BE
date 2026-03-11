package com.project.nyamtori.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.nyamtori.domain.Ingredient;
import com.project.nyamtori.domain.RecipeJob;
import com.project.nyamtori.dto.request.RecipeGenerateRequest;
import com.project.nyamtori.dto.response.GetRecipesResponse;
import com.project.nyamtori.dto.response.RecipeStatusResponse;
import com.project.nyamtori.enums.JobStatus;
import com.project.nyamtori.repository.IngredientRepository;
import com.project.nyamtori.repository.RecipeJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeAIService {

    private final RecipeJobRepository recipeJobRepository;
    private final ObjectMapper objectMapper;
    private final GeminiClient geminiClient;
    private final IngredientRepository ingredientRepository;

    public Long createRecipeJob(RecipeGenerateRequest request){
        RecipeJob job = new RecipeJob();
        job.setUserId(1L);
        job.setStatus(JobStatus.PENDING);
        job.setCookTime(request.getCookTime());

        RecipeJob saved = recipeJobRepository.save(job);

        generateRecipeAsync(saved.getId(),request);

        return saved.getId();
    }

    @Async
    public void generateRecipeAsync(Long jobId,RecipeGenerateRequest request){
        RecipeJob job = recipeJobRepository.findById(jobId).orElseThrow();

        job.setStatus(JobStatus.RUNNING);
        recipeJobRepository.save(job);

        List<Ingredient> ingredients=
                ingredientRepository.findAllById(request.getIngredientIds());

        List<String> names = ingredients.stream()
                .map(Ingredient::getIngredientName)
                .toList();

        String recipe = geminiClient.generateRecipe(names);;

        job.setResult(recipe);
        job.setStatus(JobStatus.COMPLETE);

        recipeJobRepository.save(job);
    }

    public RecipeStatusResponse getJobStatus(Long jobId){
        RecipeJob job= recipeJobRepository.findById(jobId)
                .orElseThrow(()-> new RuntimeException("job 없음"));

        int progress = 0;
        String message="";
        switch (job.getStatus()) {

            case PENDING -> {
                progress = 0;
                message = "레시피 생성 요청이 접수되었습니다.";
            }

            case RUNNING -> {
                progress = 60;
                message = "레시피를 생성 중입니다.";
            }

            case COMPLETE -> {
                progress = 100;
                message = "레시피 생성이 완료되었습니다.";
            }

            case FAILED -> {
                progress = 0;
                message = "AI 서버 오류로 레시피 생성에 실패했습니다.";
            }
        }
        RecipeStatusResponse.StatusData data =
                new RecipeStatusResponse.StatusData(
                        job.getId(),
                        job.getStatus(),
                        progress,
                        message
                );

        return new RecipeStatusResponse(data);


    }

    public GetRecipesResponse getRecipesList() {
        List<RecipeJob> jobs = recipeJobRepository.findAll();

        List<GetRecipesResponse.RecipeInfo> recipes = new ArrayList<>();

        for (RecipeJob job : jobs) {
            if (job.getResult() != null) {
                try {
                    JsonNode root = objectMapper.readTree(job.getResult());
                    String title = root.path("title").asText();
                    Integer cookTime = job.getCookTime();

                    recipes.add(
                            new GetRecipesResponse.RecipeInfo(
                                    job.getId(),
                                    title,
                                    cookTime,
                                    false // 찜 기능 생성 전
                            )
                    );
                } catch (Exception e) {
                    throw new RuntimeException("레시피 파싱 실패");
                }
            }

        }

        return new GetRecipesResponse(recipes);
    }
}
