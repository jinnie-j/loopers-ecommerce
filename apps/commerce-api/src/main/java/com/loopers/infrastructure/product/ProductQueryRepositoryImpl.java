package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductQueryRepository;
import com.loopers.domain.like.QLikeEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductQueryCommand;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.QProductEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductEntity> findBySortType(ProductSortType sort, Pageable pageable) {
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

        query.offset(pageable.getOffset()).limit(pageable.getPageSize());

        return query.fetch();
    }

    @Override
    public Page<ProductEntity> searchProducts(ProductQueryCommand.SearchProducts command) {
        QProductEntity qProduct = QProductEntity.productEntity;

        int page = command.pageOrDefault();
        int size = command.sizeOrDefault();

        BooleanBuilder where = new BooleanBuilder();
        if (command.hasBrandFilter()) {
            where.and(qProduct.brandId.eq(command.getBrandId()));
        }

        JPQLQuery<ProductEntity> jpqlQuery = queryFactory
                .selectFrom(qProduct)
                .where(where);

        switch (command.sortOrDefault()) {
            case LIKES_DESC -> jpqlQuery.orderBy(qProduct.likeCount.desc(), qProduct.id.desc());
            case PRICE_ASC  -> jpqlQuery.orderBy(qProduct.price.asc(),     qProduct.id.desc());
            case LATEST     -> jpqlQuery.orderBy(qProduct.createdAt.desc(), qProduct.id.desc());
        }

        List<ProductEntity> content = jpqlQuery
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Long total = queryFactory
                .select(qProduct.id.count())
                .from(qProduct)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, PageRequest.of(page, size), total == null ? 0 : total);
    }
}
