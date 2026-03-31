package com.evgeny.orderservice.Repository;


import com.evgeny.orderservice.Entity.ItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemsRepository extends JpaRepository<ItemsEntity, Long> {
}
