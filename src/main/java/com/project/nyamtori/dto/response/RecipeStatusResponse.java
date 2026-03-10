package com.project.nyamtori.dto.response;

import com.project.nyamtori.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeStatusResponse {
    private JobStatus status;
    private Object result;

}
