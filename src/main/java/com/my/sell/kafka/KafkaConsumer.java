package com.my.sell.kafka;

import com.google.gson.Gson;
import com.my.sell.model.ProductInfo;
import com.my.sell.model.SellerInfo;
import com.my.sell.service.ProductInfoService;
import com.my.sell.service.SellerInfoService;
import com.my.sell.zk.ZooKeeperSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Map;

@Component
@Slf4j
public class KafkaConsumer {


    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private SellerInfoService sellerInfoService;

    @KafkaListener(topics = "sell-cache")
    @SuppressWarnings("unchecked")
    public void listen(ConsumerRecord<String,String> record){
        String json = record.value();
        Map<String,String> map = new Gson().fromJson(json, Map.class);
        String serviceId = map.get("serviceId");
        if ("productInfoService".equals(serviceId)) {
            processProductInfoCacheMessage(map);
        }else if("sellerInfoService".equals(serviceId)){
            processSellerInfoCacheMessage(map);
        }
    }

    private void processProductInfoCacheMessage(Map<String,String> map){
        String productId = map.get("productId");
        ProductInfo productInfo = productInfoService.findOne(productId);
        if (productInfo != null) {
            ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
            //尝试加锁
            zooKeeperSession.acquireDistributedLock(productId);
            log.info("kafka主动更新拿到锁");
            //看看缓存中
            ProductInfo productInfoInCache = productInfoService.findOneInCache(productId);
            if (productInfoInCache != null) {
                //数据旧于缓存数据，则没必要更新
                if(productInfo.getUpdateTime().before(productInfoInCache.getUpdateTime())){
                    System.out.println("时间"+productInfo.getUpdateTime()+"旧于缓存数据时间"+productInfoInCache.getUpdateTime()+"，则没必要更新");
                    //不用设置缓存，释放锁，立即返回
                    zooKeeperSession.releaseDistributedLock(productId);
                    return;
                }
            }
            log.info("kafka主动更新缓存阻塞结束");
            productInfoService.setCache(productInfo);
            //释放锁
            zooKeeperSession.releaseDistributedLock(productId);
        }
    }

    private void processSellerInfoCacheMessage(Map<String,String> map){
        String sellerId = map.get("sellerId");
        SellerInfo sellerinfo = sellerInfoService.findOne(sellerId);
        if (sellerinfo != null) {
            sellerInfoService.setCache(sellerinfo);
        }
    }
}
