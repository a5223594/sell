package com.my.sell.controller.seller;

import com.google.gson.Gson;
import com.my.sell.form.ProductForm;
import com.my.sell.model.ProductCategory;
import com.my.sell.model.ProductInfo;
import com.my.sell.rebuild.RebuildCacheQueue;
import com.my.sell.request.ProductCacheRefreshRequest;
import com.my.sell.request.Request;
import com.my.sell.service.ProductCategoryService;
import com.my.sell.service.ProductInfoService;
import com.my.sell.service.RequestAsyncService;
import com.my.sell.utils.IdWorker;
import freemarker.template.utility.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/seller/product")
@Slf4j
public class SellerProductController {

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    IdWorker idWorker;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private RequestAsyncService requestAsyncService;

    @GetMapping("/list")
    public ModelAndView list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size,
                             ModelAndView mv) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ProductInfo> productInfoPage = productInfoService.findAll(pageable);
        mv.addObject("productInfoPage", productInfoPage);
        mv.addObject("currentPage",page);
        mv.addObject("size",size);
        mv.setViewName("/product/list");
        return mv;
    }

    @GetMapping(value = "/index")
    public ModelAndView index(@RequestParam(value = "productId",required = false) String productId,
                              ModelAndView mv) {
        try {
            ProductInfo productInfo;
            if (productId != null) {
                Request request = new ProductCacheRefreshRequest(productId,productInfoService);
                requestAsyncService.process(request);
                long start = System.currentTimeMillis();
                long end;
                long wait = 0L;
                while (true) {
                    //最多等待200ms，否则查数据库,因为本地性能较差，扩大10倍
                    if (wait > 5000) {
                        log.info("在缓存中读不到product{}",productId);
                        productInfo= productInfoService.findOne(productId);

                        //数据库都查不到，那么直接建一个null对象返回
                        if (productInfo == null) {
                            log.info("数据库都查不到");
                            productInfo= new ProductInfo();
                            productInfo.setProductId(productId);
                            break;
                        }
                        log.info(productInfo.toString());
                        log.info("超时后查询数据库设置缓存");
                        productInfoService.setCache(productInfo);
                        break;
                    }
                    productInfo = productInfoService.findOneInCache(productId);
                    if (productInfo != null) {
                        log.info("{}毫秒从缓存中读到product{}",wait,productId);
                        break;
                    }else{
                        Thread.sleep(20);
                        end = System.currentTimeMillis();
                        wait = end -start;
                    }
                }
                mv.addObject("productInfo", productInfo);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<ProductCategory> categoryList = productCategoryService.findAll();
        mv.addObject("categoryList", categoryList);
        mv.setViewName("/product/index");
        return mv;
    }

    @PostMapping(value = "/save")
    public ModelAndView save(@Valid ProductForm productForm, BindingResult bindingResult,
                             ModelAndView mv) {
        if (bindingResult.hasErrors()) {
            mv.addObject("msg", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
            mv.addObject("url", "/sell/seller/product/index");
            mv.setViewName("common/error");
            return mv;
        }

        ProductInfo productInfo = new ProductInfo();
        String productId = productForm.getProductId();
        //如果productId不为空，则为修改
        try {
            if (!StringUtils.isEmpty(productId)) {
                productInfo = productInfoService.findOne(productId);
            }else{
                String id = String.valueOf(idWorker.nextId()+"");
                productForm.setProductId(id);
            }
            BeanUtils.copyProperties(productForm,productInfo);
            //为新增的商品加创建时间
            if(StringUtils.isEmpty(productInfo.getCreateTime())){
                productInfo.setCreateTime(new Date());
            }
            productInfo.setUpdateTime(new Date());
            productInfoService.save(productInfo);
            //发送缓存修改信息到kafka
            log.info("发送新的productId:{}到kafka，主动更新缓存",productId);
            Map<String,String> map = new HashMap<>();
            map.put("serviceId","productInfoService");
            map.put("productId", productId);
            kafkaTemplate.send("sell-cache", new Gson().toJson(map));

        } catch (BeansException e) {
            mv.addObject("msg",e.getMessage());
            mv.addObject("url","/sell/seller/product/index");
            mv.setViewName("common/error");
            return mv;
        }
        mv.addObject("url", "/sell/seller/product/list");
        mv.setViewName("common/success");
        return mv;
    }

    @GetMapping(value = "/on_sale")
    public ModelAndView onSale(@RequestParam String productId,
                               ModelAndView modelAndView) {
        try {
            productInfoService.onSale(productId);
        } catch (Exception e) {
            modelAndView.addObject("msg", e.getMessage());
            modelAndView.addObject("url","/sell/seller/product/list");
            modelAndView.setViewName("common/error");
            return modelAndView;
        }
        modelAndView.addObject("url","/sell/seller/product/list");
        modelAndView.setViewName("common/success");
        return modelAndView;
    }

    @GetMapping(value = "off_sale")
    public ModelAndView offSale(@RequestParam String productId,
                                ModelAndView modelAndView) {
        try {
            productInfoService.offSale(productId);
        } catch (Exception e) {
            modelAndView.addObject("msg", e.getMessage());
            modelAndView.addObject("url","/sell/seller/product/list");
            modelAndView.setViewName("common/error");
            return modelAndView;
        }
        modelAndView.addObject("url","/sell/seller/product/list");
        modelAndView.setViewName("common/success");
        return modelAndView;
    }

    @GetMapping(value = "/getProductInfo")
    @ResponseBody
    public ProductInfo get(@RequestParam(value = "productId",required = false) String productId) {
        ProductInfo productInfo = productInfoService.findOneInCache(productId);
        if (productInfo == null) {
            //如果缓存查不到，推到队列里去重建缓存
            productInfo = productInfoService.findOne(productId);
            if (productInfo != null) {
                log.info("压到重建缓存队列");
                RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
                rebuildCacheQueue.putProductInfo(productInfo);
            }
        }
        return productInfo;
    }



}
