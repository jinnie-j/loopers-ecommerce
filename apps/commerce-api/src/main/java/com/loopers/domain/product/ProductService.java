package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductInfo create(ProductCommand.Create productCommand) {
        ProductEntity productEntity = ProductEntity.of(
                productCommand.name(),
                productCommand.price(),
                productCommand.stock(),
                productCommand.brandId()
        );
        return ProductInfo.from(productRepository.save(productEntity));
    }

    public ProductInfo getProduct(long productId) {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(()-> new CoreException(ErrorType.NOT_FOUND));
        return ProductInfo.from(productEntity);
    }

    public void decreaseStock(Long productId, Long quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.decreaseStock(quantity);
        productRepository.save(product);
    }
}
