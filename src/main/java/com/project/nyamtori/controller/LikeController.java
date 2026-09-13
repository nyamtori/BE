package com.project.nyamtori.controller;

import com.project.nyamtori.dto.request.LikeCreateRequest;
import com.project.nyamtori.dto.response.LikeResponse;
import com.project.nyamtori.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/myrecipe/likes")
@RequiredArgsConstructor
@Tag(name = "Like", description = "찜 관련 API")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "찜 추가", description = "레시피를 찜 목록에 추가합니다.")
    @PostMapping
    public ResponseEntity<LikeResponse> createLike(
            @AuthenticationPrincipal Long userId,
            @RequestBody LikeCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(likeService.createLike(userId, req));
    }

    @Operation(summary = "찜 단건 조회", description = "특정 찜 항목을 조회합니다.")
    @GetMapping("/{likeId}")
    public ResponseEntity<LikeResponse> getLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long likeId) {
        return ResponseEntity.ok(likeService.getLike(userId, likeId));
    }

    @Operation(summary = "찜 전체 조회", description = "유저의 찜 목록 전체를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<LikeResponse>> getAllLikes(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(likeService.getAllLikes(userId));
    }

    @Operation(summary = "찜 삭제", description = "찜 목록에서 레시피를 삭제합니다.")
    @DeleteMapping("/{likeId}")
    public ResponseEntity<Void> deleteLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long likeId) {
        likeService.deleteLike(userId, likeId);
        return ResponseEntity.noContent().build();
    }
}