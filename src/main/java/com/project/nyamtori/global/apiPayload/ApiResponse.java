package com.project.nyamtori.global.apiPayload;

import com.project.nyamtori.global.apiPayload.code.GeneralSuccessCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean isSuccess;
    private final String code;
    private final String message;
    private final T result;

    private ApiResponse(boolean isSuccess, String code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> ApiResponse<T> onSuccess(GeneralSuccessCode successCode, T result) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(), result);
    }
}