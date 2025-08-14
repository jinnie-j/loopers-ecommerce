package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_prod_brand_like_id", columnList = "brand_id, like_count DESC, id DESC"),
                @Index(name = "idx_prod_brand_created_id", columnList = "brand_id, created_at DESC, id DESC")
        }
)
@Getter
@NoArgsConstructor
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long price;
    private Long stock;
    @Column(name = "brand_id")
    private Long brandId;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    @Column(name = "like_count", nullable = false)
    private long likeCount;

    private ProductEntity(String name, Long price, Long stock, Long brandId){
        if(name == null || name.isBlank()){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if(price == null || price < 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if(stock == null || stock < 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if(brandId == null){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.brandId = brandId;
        this.status = ProductStatus.ON_SALE;
    }

    public static ProductEntity of(String name, Long price, Long stock, Long brandId){
        return new ProductEntity(name, price, stock, brandId);
    }

    public void decreaseStock(long quantity){
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "차감 수량은 0보다 커야 합니다.");
        }
        if(quantity > this.stock)
            throw new CoreException(ErrorType.BAD_REQUEST);
        this.stock -= quantity;
    }
    public void increaseStock(long quantity){
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "추가 수량은 0보다 커야 합니다.");
        }
        this.stock += quantity;
    }
}
