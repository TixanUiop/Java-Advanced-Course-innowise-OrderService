package com.evgeny.orderservice.entity;

import com.evgeny.orderservice.entity.Enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@ToString(exclude = "orderItems")
@EqualsAndHashCode(exclude = "orderItems")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class OrdersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    private boolean deleted;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    List<OrderItemsEntity> orderItems;

}
