package com.my.sell.service;

import com.my.sell.dto.CartDTO;
import com.my.sell.model.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductInfoService {

    ProductInfo findOne(String productId);

    List<ProductInfo> findAll();

    Page<ProductInfo> findAll(Pageable pageable);

    ProductInfo save(ProductInfo productInfo);

    //上架
    ProductInfo onSale(String productId);

    //下架
    ProductInfo offSale(String productId);

    List<ProductInfo> findUpAll();

    /**
     * 加库存
     */
    void increaseProductStock(List<CartDTO> cartDTOList);

    /**
     * 减库存
     */
    void decreaseProductStock(List<CartDTO> cartDTOList);

    /*
    删除缓存
     */
    void removeCache(String productId);

    ProductInfo findOneInRedis(String productId);

    /*
    从缓存中读，先读redis，读不到读ehcache
     */
    ProductInfo findOneInCache(String productId);

    /*
    设置缓存到redis和ehcache
     */
    void setCache(ProductInfo productInfo);
}
