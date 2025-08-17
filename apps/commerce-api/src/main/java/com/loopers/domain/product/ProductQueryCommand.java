package com.loopers.domain.product;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductQueryCommand {

    @Getter
    public static class SearchProducts {
        private final Long brandId;
        private final ProductSortType sort;
        private final Integer page;
        private final Integer size;

        private SearchProducts(Long brandId, ProductSortType sort, Integer page, Integer size) {
            this.brandId = brandId;
            this.sort = (sort == null) ? ProductSortType.LATEST : sort;
            this.page = (page == null || page < 0) ? 0 : page;
            int s = (size == null || size <= 0) ? 20 : size;
            this.size = Math.min(s, 100);
        }

        public static SearchProducts of(Long brandId, ProductSortType sort, Integer page, Integer size) {
            return new SearchProducts(brandId, sort, page, size);
        }

        public boolean hasBrandFilter() { return brandId != null; }
        public int pageOrDefault() { return page; }
        public int sizeOrDefault() { return size; }
        public ProductSortType sortOrDefault() { return sort; }
    }
}
