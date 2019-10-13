package com.my.sell.controller.seller;

import com.my.sell.VO.ResultVO;
import com.my.sell.constant.CookieConstant;
import com.my.sell.constant.RedisConstant;
import com.my.sell.enums.ResultEnum;
import com.my.sell.exception.SellException;
import com.my.sell.form.SellerForm;
import com.my.sell.model.SellerInfo;
import com.my.sell.service.SellerInfoService;
import com.my.sell.utils.CookieUtil;
import com.my.sell.utils.IdWorker;
import com.my.sell.utils.ResultVOUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 卖家登录登出
 */
@Controller
@RequestMapping("/seller/info")
public class SellerInfoController {

    @Autowired
    IdWorker idWorker;

    @Autowired
    SellerInfoService sellerInfoService;

    @PostMapping("/create")
    @ResponseBody
    public ResultVO create(@Valid SellerForm sellerForm,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SellException(ResultEnum.PARAM_ERROR.getCode(),
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        SellerInfo sellerInfo = new SellerInfo();
        BeanUtils.copyProperties(sellerForm,sellerInfo);
        sellerInfo.setSellerId(String.valueOf(idWorker.nextId()));
        sellerInfo.setCreateTime(new Date());
        sellerInfo.setUpdateTime(new Date());
        sellerInfoService.create(sellerInfo);
        return ResultVOUtil.success();
    }



    @GetMapping("/login")
    public ModelAndView login(@RequestParam("openid") String openid,
                              HttpServletResponse response,
                              Map<String, Object> map) {

        //1. openid去和数据库里的数据匹配
        SellerInfo sellerInfo = sellerInfoService.findSellerInfoByOpenid(openid);
        if (sellerInfo == null) {
            map.put("msg", ResultEnum.LOGIN_FAIL.getMessage());
            map.put("url", "/sell/seller/info/toLogin");
            return new ModelAndView("common/error");
        }

        //2. 设置token至redis
        String token = UUID.randomUUID().toString();
        sellerInfoService.setTokenCache(token,openid);
        CookieUtil.set(response, CookieConstant.TOKEN, token, CookieConstant.EXPIRE);
        map.put("url", "/sell/seller/order/list");
        return new ModelAndView("common/success");
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request,
                               HttpServletResponse response,
                               Map<String, Object> map) {
        //1. 从cookie里查询
        Cookie cookie = CookieUtil.get(request, CookieConstant.TOKEN);
        if (cookie != null) {
            //2. 清除redis
            sellerInfoService.delTokenCache(cookie);
            //3. 清除cookie
            CookieUtil.set(response, CookieConstant.TOKEN, null, 0);
        }

        map.put("msg", ResultEnum.LOGOUT_SUCCESS.getMessage());
        map.put("url", "/sell/seller/order/list");
        return new ModelAndView("common/success", map);
    }

    @GetMapping(value = "/getSellerInfo")
    @ResponseBody
    public SellerInfo get(@RequestParam("sellerId") String sellerId) {
        SellerInfo sellerInfo = sellerInfoService.findInCache(sellerId);
        if (sellerInfo == null) {
            sellerInfo = sellerInfoService.findOne(sellerId);
            if (sellerInfo != null) {
                sellerInfoService.setCache(sellerInfo);
            }
        }
        return sellerInfo;
    }


}
