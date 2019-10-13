package com.my.sell.service.impl;

import com.my.sell.dto.CartDTO;
import com.my.sell.enums.ResultEnum;
import com.my.sell.exception.SellException;
import com.my.sell.request.ProductCacheRefreshRequest;
import com.my.sell.request.ProductDBUpdateRequest;
import com.my.sell.request.Request;
import com.my.sell.request.RequestQueue;
import com.my.sell.service.RequestAsyncService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class RequestAsyncServiceImpl implements RequestAsyncService {


    @Override
    public void process(Request request) {
        try {
            RequestQueue requestQueue = RequestQueue.getInstance();
            Map<String,Boolean> flagMap = requestQueue.getFlagMap();
            if(request instanceof ProductDBUpdateRequest){
                List<CartDTO> cartDTOList = request.getCartDTOList();
                for (CartDTO cartDTO : cartDTOList) {
                    flagMap.put(cartDTO.getProductId(), true);
                    ArrayBlockingQueue<Request> routingQueue = getQueue(cartDTO.getProductId());
                    routingQueue.put(request);
                }
            }else if(request instanceof ProductCacheRefreshRequest){

                //如果队列里没有写请求，则flag= false,第一个读请求读不到缓存会走查数据库，设置缓存
                Boolean flag = flagMap.putIfAbsent(request.getProductId(), false);
                //如果队列里有写请求，不用多个读请求排队，设置下一个读请求不需要排队
                if(flag!=null && flag){
                    flagMap.put(request.getProductId(), false);
                }
                //对于已经有读请求排队的情况 直接返回
                if (flag!=null && !flag) {
                    return;
                }
                ArrayBlockingQueue<Request> routingQueue = getQueue(request.getProductId());
                routingQueue.put(request);
            }
        } catch (InterruptedException e) {
            throw new SellException(ResultEnum.PRODUCT_STOCK_ERROR);
        }
    }

    /**
     * 根据productId做hash路由到相应到队列
     * @param productId 商品id
     * @return
     */
    private ArrayBlockingQueue<Request> getQueue(String productId) {
        RequestQueue requestQueue = RequestQueue.getInstance();
        int h;
        int hash = (h = productId.hashCode())^h >>>16;
        int index = (requestQueue.size()-1)&hash;
        return requestQueue.getQueue(index);
    }
}
