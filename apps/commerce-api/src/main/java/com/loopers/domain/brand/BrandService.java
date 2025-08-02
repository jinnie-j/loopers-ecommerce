package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandInfo getBrand(Long brandId) {
        BrandEntity brandEntity = brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[id = " + brandId + "] 브랜드를 찾을 수 없습니다."));
        return  BrandInfo.from(brandEntity);
    }

    public BrandInfo create(BrandCommand.Create brandCommand) {
        BrandEntity brandEntity = BrandEntity.of(
                brandCommand.name(),
                brandCommand.description()
        );
        BrandEntity savedEntity = brandRepository.save(brandEntity);
        return BrandInfo.from(savedEntity);
    }


    public String getBrandName(Long brandId) {
        return brandRepository.findById(brandId)
                .map(BrandEntity::getName)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드 정보를 찾을 수 없습니다."));
    }
}
