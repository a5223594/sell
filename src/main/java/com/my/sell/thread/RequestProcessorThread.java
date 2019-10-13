package com.my.sell.thread;

import com.my.sell.request.Request;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

public class RequestProcessorThread implements Callable<Boolean> {

    private final ArrayBlockingQueue<Request> queue;

    public RequestProcessorThread(ArrayBlockingQueue<Request> queue) {
        this.queue = queue;
    }
    @Override
    public Boolean call() throws Exception {

        try {
            while (true) {
                //请求从内存队列里取，队列如果是空的，或者是满的，会阻塞在这
                Request request = queue.take();
                request.process();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
}
