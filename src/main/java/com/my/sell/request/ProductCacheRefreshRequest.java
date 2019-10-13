package com.my.sell.request;

import com.my.sell.dto.CartDTO;
import com.my.sell.enums.ResultEnum;
import com.my.sell.exception.SellException;
import com.my.sell.model.ProductInfo;
import com.my.sell.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.stream.events.EndDocument;
import java.util.List;

/*
商品库存缓存刷新请求
 */
@Slf4j
public class ProductCacheRefreshRequest implements Request {

    private String productId;

    private ProductInfoService productInfoService;

    public ProductCacheRefreshRequest(String productId,ProductInfoService productInfoService) {
        this.productId = productId;
        this.productInfoService = productInfoService;
    }

    @Override
    public void process() {
        ProductInfo productInfo;
        productInfo= productInfoService.findOneInCache(productId);
        if(productInfo == null) {
            log.info("读请求排队了");
            long start = System.currentTimeMillis();
            log.info("ProductCacheRefreshRequest查数据库,start:"+ start);
            productInfo = productInfoService.findOne(productId);
            long end = System.currentTimeMillis();
            log.info("ProductCacheRefreshRequest查数据库,end:"+ end);
            log.info("查数据库用了几毫秒："+(end-start));
            if (productInfo == null) {
                productInfo = new ProductInfo();
                productInfo.setProductId(productId);
            }
            long start1 = System.currentTimeMillis();
            log.info("ProductCacheRefreshRequest设置缓存,start:"+ start1);
            productInfoService.setCache(productInfo);

            long end1= System.currentTimeMillis();
            log.info("ProductCacheRefreshRequest设置缓存,end:"+ end1);
            log.info("设置缓存用了几毫秒："+(end1-start1));
        }
    }

    @Override
    public String getProductId() {
        return productId;
    }

    @Override
    public List<CartDTO> getCartDTOList() {
        return null;
    }
}
