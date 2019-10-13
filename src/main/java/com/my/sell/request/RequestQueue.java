package com.my.sell.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class RequestQueue {

    private List<ArrayBlockingQueue<Request>> requestQueues
            = new ArrayList<>();

    private Map<String, Boolean> flagMap
            = new ConcurrentHashMap<>();

    public static RequestQueue getInstance() {
        return Instance.getInstance();
    }

    public Map<String, Boolean> getFlagMap() {
        return flagMap;
    }

    public void addQueue(ArrayBlockingQueue<Request> queue) {
        this.requestQueues.add(queue);
    }

    public ArrayBlockingQueue<Request> getQueue(int index) {
        return requestQueues.get(index);
    }

    public int size() {
        return requestQueues.size();
    }


    private static class Instance {
        private static RequestQueue requestQueue
            = new RequestQueue();

        private static RequestQueue getInstance() {
            return requestQueue;
        }
    }
}
