package com.project.nyamtori.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.nyamtori.domain.Ingredient;
import com.project.nyamtori.domain.RecipeJob;
import com.project.nyamtori.dto.request.RecipeGenerateRequest;
import com.project.nyamtori.dto.response.RecipeStatusResponse;
import com.project.nyamtori.enums.JobStatus;
import com.project.nyamtori.repository.IngredientRepository;
import com.project.nyamtori.repository.RecipeJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        try{
            job.setIngredientsJsons(objectMapper.writeValueAsString(request.getIngredientIds()));
        } catch (Exception e){
            throw new RuntimeException("Json 변환 실패");
        }

        RecipeJob saved = recipeJobRepository.save(job);

        List<Ingredient> ingredients=
                ingredientRepository.findAllById(request.getIngredientIds());

        List<String> names = ingredients.stream()
                .map(Ingredient::getIngredientName)
                .toList();

        String recipe = geminiClient.generateRecipe(names);;

        job.setResult(recipe);

        job.setStatus(JobStatus.COMPLETE);
        recipeJobRepository.save(job);

        return saved.getId();
    }

    public RecipeStatusResponse getJobStatus(Long jobId){
        RecipeJob job= recipeJobRepository.findById(jobId)
                .orElseThrow(()-> new RuntimeException("job 없음"));

        return new RecipeStatusResponse(
                job.getStatus(),
                job.getResult()
        );
    }
}
