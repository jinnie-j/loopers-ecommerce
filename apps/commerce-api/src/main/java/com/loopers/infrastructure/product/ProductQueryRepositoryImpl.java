package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductQueryRepository;
import com.loopers.domain.like.QLikeEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.QProductEntity;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductEntity> findBySortType(ProductSortType sort) {
        QProductEntity product = QProductEntity.productEntity;
        QLikeEntity like = QLikeEntity.likeEntity;

        JPQLQuery<ProductEntity> query = queryFactory
                .selectFrom(product);

        switch (sort) {
            case LATEST -> query.orderBy(product.createdAt.desc());
            case PRICE_ASC -> query.orderBy(product.price.asc());
            case LIKES_DESC -> {
                query
                        .leftJoin(like).on(like.productId.eq(product.id))
                        .groupBy(product.id)
                        .orderBy(like.count().desc());
            }
        }

        return query.fetch();
    }
}
