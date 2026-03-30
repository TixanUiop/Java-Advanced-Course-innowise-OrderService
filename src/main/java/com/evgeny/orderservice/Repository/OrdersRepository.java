package com.evgeny.orderservice.Repository;

import com.evgeny.orderservice.Entity.OrdersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrdersRepository extends JpaRepository<OrdersEntity, Long>, JpaSpecificationExecutor<OrdersEntity> {

    List<OrdersEntity> findByUserId(Long userId);



}
