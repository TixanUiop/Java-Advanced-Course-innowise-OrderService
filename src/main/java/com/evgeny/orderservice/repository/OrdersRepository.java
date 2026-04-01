package com.evgeny.orderservice.repository;

import com.evgeny.orderservice.entity.OrdersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<OrdersEntity, Long>, JpaSpecificationExecutor<OrdersEntity> {

    List<OrdersEntity> findByUserId(Long userId);

}
