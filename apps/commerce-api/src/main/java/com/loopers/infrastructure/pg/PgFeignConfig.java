package com.loopers.infrastructure.pg;

import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class PgFeignConfig {
    @Bean
    Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    RequestInterceptor userIdHeader(@Value("${pg.user-id}") String userId) {
        return template -> template.header("X-USER-ID", userId);
    }

}
