package com.project.nyamtori.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.nyamtori.domain.*;
import com.project.nyamtori.dto.request.RecipeGenerateRequest;
import com.project.nyamtori.dto.response.GetRecipesResponse;
import com.project.nyamtori.dto.response.RecipeAIResponse;
import com.project.nyamtori.dto.response.RecipeResponse;
import com.project.nyamtori.dto.response.RecipeStatusResponse;
import com.project.nyamtori.enums.JobStatus;
import com.project.nyamtori.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeAIService {

    private final RecipeJobRepository recipeJobRepository;
    private final ObjectMapper objectMapper;
    private final GeminiClient geminiClient;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

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


        String recipeJson = geminiClient.generateRecipe(names);

        RecipeAIResponse aiResponse;

        try {
            aiResponse = objectMapper.readValue(recipeJson, RecipeAIResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 레시피 파싱 실패", e);
        }

        for (RecipeAIResponse.RecipeItem item : aiResponse.getRecipes()) {

            Recipe recipe = new Recipe();
            recipe.setFood(item.title());
            recipe.setRecipe(String.join("\n", item.steps()));
            recipe.setCookTime(request.getCookTime());

            recipeRepository.save(recipe);

            for (RecipeAIResponse.IngredientItem ingredientItem : item.ingredients()) {

                Ingredient ingredient = ingredientRepository
                        .findByIngredientName(ingredientItem.name())
                        .orElseThrow();

                RecipeIngredient recipeIngredient =
                        new RecipeIngredient(recipe, ingredient, ingredientItem.amount());

                recipeIngredientRepository.save(recipeIngredient);
            }


        }

        job.setResult(recipeJson);
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

    // 레시피 리스트 전체 조회
    public GetRecipesResponse getRecipesList(Long kakaoId) {
        User user = userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        List<Recipe> recipes = recipeRepository.findAll();

        List<Like> likes = likeRepository.findAllByUser(user);

        Set<Long> likedRecipeIds = likes.stream()
                .map(like -> like.getRecipe().getRecipeId())
                .collect(Collectors.toSet());

        List<GetRecipesResponse.RecipeInfo> result = new ArrayList<>();

        for (Recipe recipe : recipes) {

            boolean liked = likedRecipeIds.contains(recipe.getRecipeId());

            result.add(
                    new GetRecipesResponse.RecipeInfo(
                            recipe.getRecipeId(),
                            recipe.getFood(),
                            recipe.getCookTime(),
                            liked
                    )
            );
        }

        return new GetRecipesResponse(result);
    }

    // 단건 레시피 상세 조회
    public RecipeResponse recipe(Long kakaoId, Long recipeId){

        User user = userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));


        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow();

        boolean liked = likeRepository.existsByUserAndRecipe_RecipeId(user,recipeId);

        List<RecipeIngredient> recipeIngredients =
                recipeIngredientRepository.findByRecipeRecipeId(recipeId);

        List<RecipeResponse.Ingredients> ingredients = new ArrayList<>();

        for(RecipeIngredient ri : recipeIngredients){
            ingredients.add(
                    new RecipeResponse.Ingredients(
                            ri.getIngredient().getIngredientId(),
                            ri.getIngredient().getIngredientName(),
                            ri.getAmount()
                    )
            );
        }

        List<String> steps =
                Arrays.asList(recipe.getRecipe().split("\n"));

        return new RecipeResponse(
                recipe.getRecipeId(),
                recipe.getFood(),
                recipe.getCookTime(),
                ingredients,
                steps,
                liked
        );

    }
}
