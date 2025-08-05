package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/brands")
@RestController
@RequiredArgsConstructor
public class BrandV1Controller implements BrandV1ApiSpec{

    private final BrandService brandService;

    @Override
    public ApiResponse<BrandResponse> create(BrandRequest.Create brandRequest) {
        BrandInfo brandInfo = brandService.create(brandRequest.toCommand());
        return ApiResponse.success(BrandResponse.from(brandInfo));
    }

    @Override
    public ApiResponse<BrandResponse> getBrand(Long brandId) {
        BrandInfo brandInfo = brandService.getBrand(brandId);
        return ApiResponse.success(BrandResponse.from(brandInfo));
    }
}
