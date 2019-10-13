package com.my.sell.zk;

import org.apache.zookeeper.*;
import org.hibernate.jdbc.Expectation;
import sun.awt.SunGraphicsCallback;

import java.util.concurrent.CountDownLatch;

public class ZooKeeperSession {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    private ZooKeeper zookeeper;

    public ZooKeeperSession(){
        try {
            //异步连接zk，所以要注册一个监听器，看是否完成连接
            this.zookeeper = new ZooKeeper(
                    "192.168.229.7:2181,192.168.229.8:2181,192.168.228.9:2181",
                    50000,
                    new ZooKeeperWatcher());
            System.out.println(zookeeper.getState());
            try {
                connectedSemaphore.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("ZooKeeper会话建立完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    注册监听器，当zk删除结点时，会回调process函数
     */
    private class ZooKeeperWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            System.out.println("Receive watched event: ");
            //如果已经连接zk，则countdown
            if (Event.KeeperState.SyncConnected == event.getState()) {
                connectedSemaphore.countDown();
            }
        }
    }

    public void acquireDistributedLock(String productId) {
        String path = "/product-lock-"+productId;
        try {
            zookeeper.create(path, "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("成功获取锁: "+path);
        } catch (Exception e) {
            int count = 0;
            while (true) {
                try {
                    Thread.sleep(200);
                    zookeeper.create(path, "".getBytes(),
                            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e2) {
                    count++;
                    continue;
                }
                System.out.println("尝试"+count+"次后获取到锁");
                break;
            }
        }
    }

    public void releaseDistributedLock(String productId) {
        String path = "/product-lock-"+productId;
        try {
            zookeeper.delete(path, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private static class Singleton{
        private static ZooKeeperSession instance
                = new ZooKeeperSession();
        public static ZooKeeperSession getInstance() {
            return instance;
        }
    }

    public static ZooKeeperSession getInstance() {
        return Singleton.getInstance();
    }

    public static void init() {
        getInstance();
    }
}
