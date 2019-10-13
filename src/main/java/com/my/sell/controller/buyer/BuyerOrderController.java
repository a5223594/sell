package com.my.sell.controller.buyer;

import com.my.sell.VO.ResultVO;
import com.my.sell.converter.OrderForm2OrderDTOConverter;
import com.my.sell.dto.OrderDTO;
import com.my.sell.enums.ResultEnum;
import com.my.sell.exception.SellException;
import com.my.sell.form.OrderForm;
import com.my.sell.service.BuyerService;
import com.my.sell.service.OrderService;
import com.my.sell.utils.ResultVOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequestMapping("/buyer/order")
@RestController
@CrossOrigin
public class BuyerOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BuyerService buyerService;

    @PostMapping("/create")
    public ResultVO create(@Valid OrderForm orderForm,
                                                BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            throw new SellException(ResultEnum.PARAM_ERROR.getCode(),
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        OrderDTO orderDTO = OrderForm2OrderDTOConverter.convert(orderForm);
        if (CollectionUtils.isEmpty(orderDTO.getOrderDetailList())) {
            throw new SellException(ResultEnum.CART_EMPTY);
        }
        OrderDTO result = orderService.create(orderDTO);
        Map<String, String> map = new HashMap<>();
        map.put("orderId", result.getOrderId());
        return ResultVOUtil.success(map);
    }

    @GetMapping("/list")
    public ResultVO list(@RequestParam String openid,
                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        if (StringUtils.isEmpty(openid)) {
            throw new SellException(ResultEnum.PARAM_ERROR);
        }
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        List<OrderDTO> orderDTOList = orderService.findList(openid, pageRequest);
        return ResultVOUtil.success(orderDTOList);
    }

    @GetMapping("/detail")
    public ResultVO detail(@RequestParam String openid, @RequestParam String orderId) {
        OrderDTO order = buyerService.findOrder(openid, orderId);
        return ResultVOUtil.success(order);
    }

    @PostMapping("/cancel")
    public ResultVO cancel(@RequestParam String openid,
                           @RequestParam String orderId) {
        buyerService.cancelOrder(openid, orderId);
        return ResultVOUtil.success();
    }
}
