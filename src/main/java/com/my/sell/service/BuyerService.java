package com.my.sell.service;

import com.my.sell.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;

public interface BuyerService {

    OrderDTO findOrder(String openid, String orderId);

    OrderDTO cancelOrder(String openid, String orderId);

    OrderDTO checkOrderOwner(String openid, String orderId);
}
