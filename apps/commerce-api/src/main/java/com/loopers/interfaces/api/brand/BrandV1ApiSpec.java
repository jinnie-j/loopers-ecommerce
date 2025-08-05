package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Brand API")
public interface BrandV1ApiSpec {

    @Operation(summary = "브랜드 생성")
    ApiResponse<BrandResponse> create(@RequestBody BrandRequest.Create brandRequest);

    @GetMapping("/{brandId}")
    ApiResponse<BrandResponse> getBrand(
            @Schema(description = "조회할 브랜드 ID") @PathVariable Long brandId);
}
