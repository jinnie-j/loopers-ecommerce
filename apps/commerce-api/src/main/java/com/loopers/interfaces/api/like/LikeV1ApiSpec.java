package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Like API")
@RequestMapping("/api/v1/likes")
public interface LikeV1ApiSpec {

    @GetMapping("/products")
    ResponseEntity<ApiResponse<List<LikeResponse>>> getLikesByUserId(
            @RequestHeader("X-USER-ID") Long userId
    );

    @PostMapping("/products/{productId}")
    ResponseEntity<ApiResponse<LikeResponse>> like(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId
    );

    @DeleteMapping("/products/{productId}")
    ResponseEntity<ApiResponse<LikeResponse>> unlike(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId
    );
}
