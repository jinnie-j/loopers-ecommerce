package com.loopers.interfaces.api.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class LikeV1Controller implements LikeV1ApiSpec {

    private final LikeService likeService;

    @Override
    public ResponseEntity<ApiResponse<List<LikeResponse>>> getLikesByUserId(Long userId) {
        List<LikeResponse> likes = likeService.getLikesByUserId(userId).stream()
                .map(LikeResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(likes));
    }

    @Override
    public ResponseEntity<ApiResponse<LikeResponse>> like(Long userId, Long productId) {
        LikeInfo likeInfo = likeService.like(new LikeCommand.Create(userId, productId));
        return ResponseEntity.ok(ApiResponse.success(LikeResponse.from(likeInfo)));
    }

    @Override
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(Long userId, Long productId) {
        LikeInfo unlikeInfo = likeService.unlike(new LikeCommand.Create(userId, productId));
        return ResponseEntity.ok(ApiResponse.success(LikeResponse.from(unlikeInfo)));
    }
}
