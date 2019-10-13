package com.my.sell.request;

import com.my.sell.dto.CartDTO;
import com.my.sell.model.ProductInfo;

import java.util.List;

public interface Request {

    void process();


    String getProductId();

    List<CartDTO> getCartDTOList();
}
