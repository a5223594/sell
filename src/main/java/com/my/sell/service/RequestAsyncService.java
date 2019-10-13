package com.my.sell.service;

import com.my.sell.request.Request;

/**
 * 请求转发异步服务
 */
public interface RequestAsyncService {

    void process(Request request);
}
