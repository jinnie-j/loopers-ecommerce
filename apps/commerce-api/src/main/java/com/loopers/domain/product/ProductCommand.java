package com.loopers.domain.product;

public class ProductCommand {

    public record Create(
        String name,
        Long brandId,
        Long price,
        Long stock
    ){}
}
