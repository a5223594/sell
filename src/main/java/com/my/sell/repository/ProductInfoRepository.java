package com.my.sell.repository;

import com.my.sell.model.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductInfoRepository extends JpaRepository<ProductInfo,String> {

    List<ProductInfo> findProductInfosByProductStatus(Integer status);
}
