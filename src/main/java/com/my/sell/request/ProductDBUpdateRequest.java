package com.my.sell.request;

import com.my.sell.dto.CartDTO;
import com.my.sell.exception.SellException;
import com.my.sell.model.ProductInfo;
import com.my.sell.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/*
* 商品哭库存增加或删除请求
 */
@Slf4j
public class ProductDBUpdateRequest implements Request {

    private List<CartDTO> cartDTOList;

    private ProductInfoService productInfoService;

    private AtomicBoolean increase = new AtomicBoolean(false);

    public ProductDBUpdateRequest(List<CartDTO> cartDTOList, ProductInfoService productInfoService, Boolean flag) {
        this.cartDTOList = cartDTOList;
        this.productInfoService = productInfoService;
        /*
        根据flag来判断是增加库存请求还是，减少库存请求
        true:增加库存
        false：减少库存
         */
        this.increase.compareAndSet(increase.get(),flag);
    }

    @Override
    public void process() {
        //增加库存操作
        if(increase.get()){
            for (CartDTO cartDTO : cartDTOList) {
                //删除缓存
                productInfoService.removeCache(cartDTO.getProductId());
            }

            productInfoService.increaseProductStock(cartDTOList);
        }
        //减少库存操作
        else{
            for (CartDTO cartDTO : cartDTOList) {
                //删除缓存
                productInfoService.removeCache(cartDTO.getProductId());
                log.info("删除缓存product:{}",cartDTO.getProductId());
            }

            productInfoService.decreaseProductStock(cartDTOList);
        }
    }

    @Override
    public String getProductId() {
        return null;
    }

    @Override
    public List<CartDTO> getCartDTOList() {
        return cartDTOList;
    }


}
