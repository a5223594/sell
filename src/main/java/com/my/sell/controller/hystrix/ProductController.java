package com.my.sell.controller.hystrix;

import com.my.sell.model.ProductInfo;
import com.my.sell.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProductController {


    @Autowired
    private ProductInfoService productInfoService;

    @GetMapping("/get")
    public String getProduct(String productId) {
        ProductInfo one = productInfoService.findOne(productId);
        log.info(one.toString());
        return "success";
    }
}
