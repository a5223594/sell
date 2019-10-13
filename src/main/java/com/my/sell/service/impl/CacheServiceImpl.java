package com.my.sell.service.impl;

import com.my.sell.model.ProductInfo;
import com.my.sell.model.SellerInfo;
import com.my.sell.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    private static final String CACHE_NAME = "local";

    @Override
    @CachePut(value = CACHE_NAME, key = "'product_info_'+ #productInfo.getProductId()")
    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'product_info_'+#id")
    public ProductInfo getProductInfoFromLocalCache(String id) {
        return null;
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "'key_'+#sellerInfo.getSellerId()")
    public SellerInfo saveSellerInfo2LocalCache(SellerInfo sellerInfo) {
        return sellerInfo;
    }

    @Override
    @Cacheable(value = CACHE_NAME,key ="'key_'+#id")
    public SellerInfo getSellerInfoFromLocalCache(String id) {
        return null;
    }
}
