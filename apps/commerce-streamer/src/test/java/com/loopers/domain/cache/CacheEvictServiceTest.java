package com.loopers.domain.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheEvictServiceTest {

    @Mock
    StringRedisTemplate redis;
    CacheEvictService service;

    @BeforeEach
    void setUp() {
        service = new CacheEvictService(redis);
        ReflectionTestUtils.setField(service, "productDetail", "product:detail");
        ReflectionTestUtils.setField(service, "productList",   "product:list");
    }

    @Test
    void onLikeChanged_should_delete_detail_and_list() {
        long productId = 1014L;

        when(redis.keys("product:list*"))
                .thenReturn(Set.of("product:list:all", "product:list:page:1"));

        service.onLikeChanged(productId);

        // 상세키 삭제 확인
        verify(redis).delete("product:detail:" + productId);

        // 목록키 일괄 삭제 확인
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(redis).delete(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder("product:list:all", "product:list:page:1");

        verifyNoMoreInteractions(redis);
    }

    @Test
    void onStockAdjusted_should_delete_detail_and_list_even_when_no_list_keys() {
        long productId = 2002L;

        when(redis.keys("product:list*")).thenReturn(Set.of());

        service.onStockAdjusted(productId);

        verify(redis).delete("product:detail:" + productId);

        verify(redis, never()).delete(Set.of());
        verifyNoMoreInteractions(redis);
    }
}
