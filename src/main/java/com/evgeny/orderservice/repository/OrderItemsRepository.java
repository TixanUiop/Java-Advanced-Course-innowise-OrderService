package com.evgeny.orderservice.repository;


import com.evgeny.orderservice.entity.OrderItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItemsEntity, Long> {
}
