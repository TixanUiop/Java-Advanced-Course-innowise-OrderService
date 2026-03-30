package com.evgeny.orderservice.Specification;

import com.evgeny.orderservice.Entity.Enums.OrderStatus;
import com.evgeny.orderservice.Entity.OrdersEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecifications {

    public static Specification<OrdersEntity> statusIn(List<OrderStatus> statuses) {

        return (root, query, criteriaBuilder) -> {
            if (statuses != null || statuses.isEmpty()) return null;

            return root.get("status").in(statuses);
        };
    }

    public static Specification<OrdersEntity> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<OrdersEntity> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }


}
