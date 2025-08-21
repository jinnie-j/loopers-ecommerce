package com.loopers.infrastructure.payment.pg;

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

    @Bean
    feign.codec.ErrorDecoder pgErrorDecoder() {
        return (methodKey, resp) -> {
            int s = resp.status();
            if (s == 503 || (s >= 500 && s < 600)) {
                return new feign.RetryableException(s, "server error",
                        resp.request().httpMethod(), (Long) null, resp.request());
            }
            return feign.FeignException.errorStatus(methodKey, resp);
        };
    }
}
