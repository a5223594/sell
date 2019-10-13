package com.my.sell.service;

import com.my.sell.model.ProductCategory;

import java.util.List;

public interface ProductCategoryService {

    ProductCategory findOne(Integer categoryId);

    List<ProductCategory> findAll();

    /**
     * 根据多个商品类目编号查找商品类目
     * @param categoryTypeList 商品类目编号
     * @return 商品类目列表
     */
    List<ProductCategory> findByCategoryTypeIn(List<Integer> categoryTypeList);

    ProductCategory save(ProductCategory productCategory);
}
