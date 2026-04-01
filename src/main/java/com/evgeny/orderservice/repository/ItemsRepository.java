package com.evgeny.orderservice.repository;


import com.evgeny.orderservice.entity.ItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemsRepository extends JpaRepository<ItemsEntity, Long> {
    boolean existsByName(String name);
}
