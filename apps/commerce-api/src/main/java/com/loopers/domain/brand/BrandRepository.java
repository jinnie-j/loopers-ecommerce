package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandRepository {
    Optional<BrandEntity> findById(long brandId);

    BrandEntity save(BrandEntity brand);
}
