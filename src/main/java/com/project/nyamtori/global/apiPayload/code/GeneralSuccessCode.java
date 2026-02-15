package com.project.nyamtori.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralSuccessCode {
    OK(HttpStatus.OK,"COMMON2000","성공입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
