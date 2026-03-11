package com.project.nyamtori.controller;

import com.project.nyamtori.dto.request.RecipeGenerateRequest;
import com.project.nyamtori.dto.response.GetRecipesResponse;
import com.project.nyamtori.dto.response.RecipeJobResponse;
import com.project.nyamtori.dto.response.RecipeStatusResponse;
import com.project.nyamtori.service.RecipeAIService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mykitchen")
public class RecipeController {

    private final RecipeAIService recipeAIService;

    @PostMapping("/recipes")
    @Operation(
            summary="AI 레시피 생성",
            description = "재료들을 선택하여 레시피를 생성합니다"
    )public RecipeJobResponse generateRecipe(@RequestBody RecipeGenerateRequest request){
        Long jobId=recipeAIService.createRecipeJob(request);
        return new RecipeJobResponse(jobId.toString());
    }

    @GetMapping("/recipe-jobs/{jobId}/status")
    @Operation(
            summary = "AI 레시피 생성 상태 조회",
            description = "생성시킨 레시피의 상태를 조회합니다"
    )public RecipeStatusResponse getStatus(@PathVariable Long jobId){
        return recipeAIService.getJobStatus(jobId);
    }

    @GetMapping("/recipes")
    @Operation(
            summary = "레시피 리스트 전체 조회",
            description = "생성된 레시피의 리스트를 전체 조회합니다."
    )public GetRecipesResponse getReipes(){
        return recipeAIService.getRecipesList();

    }

}
