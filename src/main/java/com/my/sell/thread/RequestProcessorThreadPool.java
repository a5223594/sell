package com.my.sell.thread;

import com.my.sell.request.Request;
import com.my.sell.request.RequestQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestProcessorThreadPool {

    private ExecutorService pool
            = Executors.newFixedThreadPool(10);

    private RequestProcessorThreadPool() {

        RequestQueue requestQueue = RequestQueue.getInstance();
        for (int i = 0; i < 10; i++) {
            ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<>(100);
            requestQueue.addQueue(queue);
            //添加任务到工作线程
            pool.submit(new RequestProcessorThread(queue));
        }
    }

    public static RequestProcessorThreadPool getInstance() {
        return Instance.instance;
    }

    public static void init() {
        getInstance();
    }

    private static class Instance {
        private static RequestProcessorThreadPool instance
                = new RequestProcessorThreadPool();;

    }
}
