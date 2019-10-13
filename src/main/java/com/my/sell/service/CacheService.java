package com.my.sell.service;


import com.my.sell.model.ProductInfo;
import com.my.sell.model.SellerInfo;

public interface CacheService {

    /**
     * 将商品信息保存到本地缓存中
     */
    ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo);

    /**
     * 从本地缓存中获取商品信息

     */
    ProductInfo getProductInfoFromLocalCache(String id);

    SellerInfo saveSellerInfo2LocalCache(SellerInfo sellerInfo);

    SellerInfo getSellerInfoFromLocalCache(String id);
}
