package com.project.nyamtori.dto.response;

import com.project.nyamtori.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeStatusResponse {
    private StatusData data;

    public record StatusData(
           Long jobId,
           JobStatus status,
           Integer progress,
           String message
    ){}

}
