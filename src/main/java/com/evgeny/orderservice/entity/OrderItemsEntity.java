package com.evgeny.orderservice.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "order_items")
@ToString(exclude = "order")
@EqualsAndHashCode(exclude = "order")
@SQLDelete(sql = "UPDATE order_items SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class OrderItemsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemsEntity item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrdersEntity order;

    private Long quantity;

    @Column(nullable = false)
    private Boolean deleted = false;
}
