package com.test;


import com.google.gson.Gson;
import com.my.sell.Application;
import com.my.sell.model.ProductInfo;
import com.my.sell.service.ProductCategoryService;
import com.my.sell.service.ProductInfoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TestRepository {


    @Autowired
    ProductInfoService service;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Test
    public void test1() {
        ProductInfo one = service.findOne("1170919816849133568");
        String s = new Gson().toJson(one);
        kafkaTemplate.send("test",s);
    }
}
