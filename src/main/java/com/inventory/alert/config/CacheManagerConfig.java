package com.inventory.alert.config;

import com.inventory.alert.constants.CacheNames;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheManagerConfig {

    @Bean
    CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new ConcurrentMapCache(CacheNames.PRODUCTS_BY_ID),
                new ConcurrentMapCache(CacheNames.PRODUCTS_BY_SKU),
                new ConcurrentMapCache(CacheNames.PRODUCT_SEARCH),
                new ConcurrentMapCache(CacheNames.LOW_STOCK)));
        return manager;
    }
}
