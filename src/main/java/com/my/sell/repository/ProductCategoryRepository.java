package com.my.sell.repository;

import com.my.sell.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Integer> {

    List<ProductCategory> findProductCategoriesByCategoryIdIn(List<Integer> categoryTypeList);
}
