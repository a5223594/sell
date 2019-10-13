package com.my.sell.rebuild;

import com.my.sell.model.ProductInfo;

import java.util.concurrent.ArrayBlockingQueue;

/*
商品服务重建缓存队列
 */
public class RebuildCacheQueue {
    private ArrayBlockingQueue<ProductInfo> queue = new ArrayBlockingQueue<>(1000);

    public void putProductInfo(ProductInfo productInfo) {
        try {
            queue.put(productInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProductInfo takeProductInfo() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class Singleton{

        private static RebuildCacheQueue rebuildCacheQueue = new RebuildCacheQueue();

        public static RebuildCacheQueue getInstance() {
            return rebuildCacheQueue;
        }

    }

    public static RebuildCacheQueue getInstance() {
        return Singleton.getInstance();
    }

    public static void init() {
        getInstance();
    }

}
