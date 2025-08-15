package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductRequest {

    public record Create(
            @NotBlank String name,
            @NotNull Long price,
            @NotNull Long stock,
            @NotNull Long brandId
    ) {
        public ProductCommand.Create toCommand() {
            return new ProductCommand.Create(name, price, stock, brandId);
        }
    }
}
