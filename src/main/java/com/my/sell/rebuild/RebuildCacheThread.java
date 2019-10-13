package com.my.sell.rebuild;

import com.my.sell.model.ProductInfo;
import com.my.sell.service.ProductInfoService;
import com.my.sell.spring.SpringContext;
import com.my.sell.zk.ZooKeeperSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/*
重建缓存线程
 */
@Slf4j
public class RebuildCacheThread implements Runnable {

    @Override
    public void run() {
        ProductInfoService productInfoService = (ProductInfoService) SpringContext.getApplicationContext().getBean("productInfoService");
        System.out.println("productInfoService:"+productInfoService);
        RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
        while (true) {
            //该线程一直回去取重建队列中的productinfo，如果队列为空，则会阻塞
            ProductInfo productInfo = rebuildCacheQueue.takeProductInfo();
            //加锁
            zooKeeperSession.acquireDistributedLock(productInfo.getProductId());
            log.info("被动重建拿锁");
            //从redis中拿
            log.info(productInfo.getProductId());
            ProductInfo productInfoInCache = productInfoService.findOneInRedis(productInfo.getProductId());
            log.info("productInfo"+productInfo.toString());

            if (productInfoInCache != null) {
                if (productInfo.getUpdateTime().before(productInfoInCache.getUpdateTime())) {
                    System.out.println("重建缓存失败，没必要重建");
                    zooKeeperSession.releaseDistributedLock(productInfo.getProductId());
                    continue;
                }
            }
            productInfoService.setCache(productInfo);
            zooKeeperSession.releaseDistributedLock(productInfo.getProductId());
            System.out.println("重建缓存成功");
        }
    }
}
