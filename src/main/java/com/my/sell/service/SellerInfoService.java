package com.my.sell.service;

import com.my.sell.model.ProductInfo;
import com.my.sell.model.SellerInfo;

import javax.servlet.http.Cookie;

public interface SellerInfoService {

    SellerInfo create(SellerInfo sellerInfo);

    SellerInfo findSellerInfoByOpenid(String openid);

    void setTokenCache(String token,String openid);

    void delTokenCache(Cookie cookie);

    SellerInfo findOne(String sellerId);

    void setCache(SellerInfo sellerinfo);

    SellerInfo findInCache(String sellerId);
}
