package com.my.sell.controller;

import com.my.sell.model.ProductInfo;
import com.my.sell.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaProducer {

    @Autowired
    ProductInfoService service;

    @Autowired
    KafkaTemplate<Object, String> kafkaTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    private static String CACHE_PREFIX = "PRODUCT:";

    @Autowired
    ProductInfoService productInfoService;

    @RequestMapping("/send")
    public String test1() {

        return "success";
    }
}
