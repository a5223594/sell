package com.my.sell.repository;

import com.my.sell.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail,String> {

    List<OrderDetail> findOrderDetailsByOrderId(String orderId);

    void deleteOrderDetailsByOrderIdIn(List<String> orderIdList);
}
