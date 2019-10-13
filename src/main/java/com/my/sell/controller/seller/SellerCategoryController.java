package com.my.sell.controller.seller;

import com.my.sell.form.CategoryForm;
import com.my.sell.model.ProductCategory;
import com.my.sell.service.ProductCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 卖家商品类目Contoller
 */
@Controller
@RequestMapping("/seller/category")
public class SellerCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @RequestMapping("/index")
    public ModelAndView index(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                              ModelAndView mv) {
        if (categoryId != null) {
            ProductCategory category = productCategoryService.findOne(categoryId);
            mv.addObject("category",category);
        }
        mv.setViewName("/category/index");
        return mv;
    }

    @GetMapping(value = "/list")
    public ModelAndView list(ModelAndView mv){
        List<ProductCategory> categoryList = productCategoryService.findAll();
        mv.addObject("categoryList",categoryList);
        mv.setViewName("/category/list");
        return mv;
    }

    /**
     * 根据category是否为空，选择新增或修改操作
     * @param categoryForm 类别表单
     * @param bindingResult 校验
     * @param mv modelAndView
     * @return modelAndView
     */
    @PostMapping("/save")
    public ModelAndView save(@Valid CategoryForm categoryForm, BindingResult bindingResult,
                             ModelAndView mv) {
        if (bindingResult.hasErrors()) {
            mv.addObject("msg", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
            mv.addObject("url","/sell/seller/category/index");
            mv.setViewName("common/error");
            return mv;
        }
        ProductCategory productCategory = new ProductCategory();
        Integer categoryId = categoryForm.getCategoryId();
        try {
            if (categoryId != null) {
                productCategory = productCategoryService.findOne(categoryId);
            }
            BeanUtils.copyProperties(categoryForm,productCategory);
            productCategory.setUpdateTime(new Date());
            productCategoryService.save(productCategory);
        } catch (BeansException e) {
            mv.addObject("msg", e.getMessage());
            mv.addObject("url","/sell/seller/category/index");
            mv.setViewName("common/error");
            return mv;
        }
        mv.addObject("url","/sell/seller/category/list");
        mv.setViewName("common/success");
        return mv;
    }

}
