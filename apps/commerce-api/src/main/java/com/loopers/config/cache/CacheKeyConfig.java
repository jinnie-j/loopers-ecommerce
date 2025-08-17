package com.loopers.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.MessageDigest;
import java.util.HexFormat;

@Configuration
public class CacheKeyConfig {

    @Bean("productListKeyGen")
    public KeyGenerator productListKeyGen(ObjectMapper om) {
        return (target, method, params) -> {

            Object command = (params.length > 0 ? params[0] : "no-arg");
            try {
                byte[] json = om.writeValueAsBytes(command);
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(json);
                return "plist:v1:" + HexFormat.of().formatHex(hash);
            } catch (Exception e) {
                return "plist:v1:" + String.valueOf(command);
            }
        };
    }
}

