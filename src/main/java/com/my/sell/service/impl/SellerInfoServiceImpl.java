package com.my.sell.service.impl;

import com.my.sell.constant.RedisConstant;
import com.my.sell.model.SellerInfo;
import com.my.sell.repository.SellerInfoRepository;
import com.my.sell.service.CacheService;
import com.my.sell.service.SellerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SellerInfoServiceImpl implements SellerInfoService {

    private static final String SELLER_CACHE_PREFIX = "SELLER:";

    @Autowired
    private SellerInfoRepository sellerInfoRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public SellerInfo create(SellerInfo sellerInfo) {
        return sellerInfoRepository.save(sellerInfo);
    }

    @Override
    public SellerInfo findSellerInfoByOpenid(String openid) {
        SellerInfo sellerInfo = sellerInfoRepository.findSellerInfoByOpenid(openid);
        if (sellerInfo != null) {
            return sellerInfo;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setTokenCache(String token,String openid) {
        redisTemplate.opsForValue().set(String.format(RedisConstant.TOKEN_PREFIX, token), openid, RedisConstant.EXPIRE, TimeUnit.SECONDS);

    }

    @Override
    public void delTokenCache(Cookie cookie) {
        redisTemplate.opsForValue().getOperations().delete(String.format(RedisConstant.TOKEN_PREFIX, cookie.getValue()));
    }

    @Override
    public SellerInfo findOne(String sellerId) {
        return sellerInfoRepository.findById(sellerId).orElse(null);
    }

    @Override
    public void setCache(SellerInfo sellerinfo) {
        log.info("设置了店铺{}redis和本地缓存",sellerinfo.getSellerId());
        redisTemplate.opsForValue().set(SELLER_CACHE_PREFIX+sellerinfo.getSellerId(),sellerinfo);
        cacheService.saveSellerInfo2LocalCache(sellerinfo);
    }

    @Override
    public SellerInfo findInCache(String sellerId) {
        SellerInfo sellerInfo = (SellerInfo) redisTemplate.opsForValue().get(SELLER_CACHE_PREFIX + sellerId);
        if (sellerInfo != null) {
            log.info("从redis中找到店铺信息{}",sellerInfo);
            return sellerInfo;
        }else{
            SellerInfo sellerInfoFromLocalCache = cacheService.getSellerInfoFromLocalCache(sellerId);
            if (sellerInfoFromLocalCache != null) {
                log.info("从ehcache中找到店铺信息{}",sellerInfoFromLocalCache);
                return sellerInfoFromLocalCache;
            }else{
                return null;
            }
        }
    }
}
