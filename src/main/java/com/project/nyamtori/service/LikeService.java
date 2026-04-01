package com.project.nyamtori.service;

import com.project.nyamtori.domain.Like;
import com.project.nyamtori.domain.Recipe;
import com.project.nyamtori.domain.User;
import com.project.nyamtori.dto.request.LikeCreateRequest;
import com.project.nyamtori.dto.response.LikeResponse;
import com.project.nyamtori.repository.LikeRepository;
import com.project.nyamtori.repository.RecipeRepository;
import com.project.nyamtori.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    // 찜 생성
    @Transactional
    public LikeResponse createLike(Long kakaoId, LikeCreateRequest req) {
        User user = userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Recipe recipe = recipeRepository.findById(req.getRecipe())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피입니다."));

        if (likeRepository.existsByUserAndRecipe_RecipeId(user, req.getRecipe())) {
            throw new IllegalStateException("이미 찜한 레시피입니다.");
        }

        Like like = Like.builder()
                .user(user)
                .recipe(recipe)
                .build();

        return LikeResponse.from(likeRepository.save(like));
    }

    // 찜 단건 조회
    public LikeResponse getLike(Long kakaoId, Long likeId) {
        User user = userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Like like = likeRepository.findByLikeIdAndUser(likeId, user)
                .orElseThrow(() -> new IllegalArgumentException("찜 내역을 찾을 수 없습니다."));

        return LikeResponse.from(like);
    }

    // 찜 전체 조회
    public List<LikeResponse> getAllLikes(Long kakaoId) {
        User user = userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return likeRepository.findAllByUser(user).stream()
                .map(LikeResponse::from)
                .collect(Collectors.toList());
    }

    // 찜 삭제
    @Transactional
    public void deleteLike(Long kakaoId, Long likeId) {
        User user = userRepository.findByKakaoId(String.valueOf(kakaoId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Like like = likeRepository.findByLikeIdAndUser(likeId, user)
                .orElseThrow(() -> new IllegalArgumentException("찜 내역을 찾을 수 없습니다."));

        likeRepository.delete(like);
    }
}