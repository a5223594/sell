package com.my.sell.service.impl;

import com.my.sell.config.WebSocket;
import com.my.sell.converter.OrderMaster2OrderDTOConverter;
import com.my.sell.dto.CartDTO;
import com.my.sell.dto.OrderDTO;
import com.my.sell.enums.OrderStatusEnum;
import com.my.sell.enums.PayStatusEnum;
import com.my.sell.enums.ResultEnum;
import com.my.sell.exception.SellException;
import com.my.sell.model.OrderDetail;
import com.my.sell.model.OrderMaster;
import com.my.sell.model.ProductInfo;
import com.my.sell.repository.OrderDetailRepository;
import com.my.sell.repository.OrderMasterRepository;
import com.my.sell.request.ProductDBUpdateRequest;
import com.my.sell.request.Request;
import com.my.sell.service.OrderService;
import com.my.sell.service.ProductInfoService;
import com.my.sell.service.RequestAsyncService;
import com.my.sell.utils.IdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMasterRepository orderMasterRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private RequestAsyncService requestAsyncService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private WebSocket webSocket;

    @Autowired
    IdWorker idWorker;

    @Override
    @Transactional
        public OrderDTO create(OrderDTO orderDTO) {
            String orderId = String.valueOf(idWorker.nextId());
            BigDecimal orderAmount = new BigDecimal(BigInteger.ZERO);
            //保存订单详情
            for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
                ProductInfo productInfo = productInfoService.findOne(orderDetail.getProductId());
                if (productInfo == null) {
                    throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
                }
                //计算订单总价
                orderAmount = productInfo.getProductPrice()
                        .multiply(new BigDecimal(orderDetail.getProductQuantity()))
                        .add(orderAmount);
                orderDetail.setDetailId(String.valueOf(idWorker.nextId()));
                orderDetail.setOrderId(orderId);
                BeanUtils.copyProperties(productInfo,orderDetail);
                orderDetailRepository.save(orderDetail);
            }
        //保存订单
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(orderId);
        BeanUtils.copyProperties(orderDTO,orderMaster);
        orderMaster.setOrderAmount(orderAmount);
        orderMaster.setCreateTime(new Date());
        orderMaster.setUpdateTime(new Date());
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());
        orderMasterRepository.save(orderMaster);

        //扣库存
        List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream()
                .map(e -> new CartDTO(e.getProductId(), e.getProductQuantity()))
                .collect(Collectors.toList());
        //封装成异步请求服务,让其到内存队列执行
        Request request = new ProductDBUpdateRequest(cartDTOList, productInfoService, false);
        requestAsyncService.process(request);
        //推送信息给卖家
        webSocket.sendMessage(orderDTO.getOrderId());
        return orderDTO;
    }

    @Override
    public OrderDTO findOne(String orderId) {
        OrderMaster orderMaster = orderMasterRepository.findById(orderId).orElse(null);
        if(orderMaster == null){
            throw new SellException(ResultEnum.ORDER_NOT_EXIST);
        }
        List<OrderDetail> orderDetailList = orderDetailRepository.findOrderDetailsByOrderId(orderId);
        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new SellException(ResultEnum.ORDERDETAIL_NOT_EXIST);
        }
        OrderDTO orderDTO = OrderMaster2OrderDTOConverter.convert(orderMaster);
        orderDTO.setOrderDetailList(orderDetailList);
        return orderDTO;
    }

    @Override
    public List<OrderDTO> findList(String buyerOpenid, Pageable pageable) {
        Page<OrderMaster> orderMastersPage = orderMasterRepository.findOrderMastersByBuyerOpenid(buyerOpenid, pageable);
        return OrderMaster2OrderDTOConverter.convert(orderMastersPage.getContent());
    }

    @Override
    @Transactional
    public OrderDTO cancel(OrderDTO orderDTO) {
        OrderMaster orderMaster = new OrderMaster();
        //判断订单状态是否等于新订单
        if(!orderDTO.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())){
            throw new SellException(ResultEnum.ORDER_STATUS_ERROR);
        }
        //将订单状态设置为取消
        orderDTO.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
        BeanUtils.copyProperties(orderDTO,orderMaster);
        //更新订单数据库
        OrderMaster updateOrder = orderMasterRepository.save(orderMaster);
        if (updateOrder == null) {
            throw new SellException(ResultEnum.ORDER_UPDATE_FAIL);
        }

        //更新成功，返回库存
        List<OrderDetail> orderDetailList = orderDTO.getOrderDetailList();
        List<CartDTO> cartDTOList = orderDetailList.stream().map(e -> new CartDTO(e.getProductId(), e.getProductQuantity()))
                .collect(Collectors.toList());
        Request request = new ProductDBUpdateRequest(cartDTOList, productInfoService, true);
        requestAsyncService.process(request);
        //不删除订单详情，只是将订单状态设置为cancel
        //List<String> orderIdList = orderDetailList.stream().map(OrderDetail::getOrderId).collect(Collectors.toList());
        //orderDetailRepository.deleteOrderDetailsByOrderIdIn(orderIdList);
        //todo 退款
        return orderDTO;
    }

    @Override
    public OrderDTO finish(OrderDTO orderDTO) {
        return null;
    }

    @Override
    public OrderDTO paid(OrderDTO orderDTO) {
        return null;
    }

    @Override
    public Page<OrderDTO> findList(Pageable pageable) {

        Page<OrderMaster> orderMastersPage = orderMasterRepository.findAll(pageable);
        List<OrderDTO> orderDTOList = OrderMaster2OrderDTOConverter.convert(orderMastersPage.getContent());

        return  new PageImpl<>(orderDTOList, pageable, orderMastersPage.getTotalElements());
    }
}
