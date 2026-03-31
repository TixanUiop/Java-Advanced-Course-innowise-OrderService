package com.evgeny.orderservice.Service;

import com.evgeny.orderservice.DTO.order.CreateOrderDTO;
import com.evgeny.orderservice.DTO.order.FullOrderDTO;
import com.evgeny.orderservice.DTO.order.OrdersSummaryDTO;
import com.evgeny.orderservice.Entity.Enums.OrderStatus;
import com.evgeny.orderservice.Entity.ItemsEntity;
import com.evgeny.orderservice.Entity.OrderItemsEntity;
import com.evgeny.orderservice.Entity.OrdersEntity;
import com.evgeny.orderservice.Mapper.order.OrderMapper;
import com.evgeny.orderservice.Repository.ItemsRepository;
import com.evgeny.orderservice.Repository.OrdersRepository;
import com.evgeny.orderservice.Specification.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderMapper orderMapper;
    private final ItemsRepository itemsRepository;

    @Override
    @Transactional
    public FullOrderDTO createOrder(CreateOrderDTO dto) {

        OrdersEntity orderEntity = OrdersEntity.builder()
                .userId(dto.getUserId())
                .status(dto.getStatus())
                .deleted(dto.isDeleted())
                .build();

        List<OrderItemsEntity> items = dto.getOrderItems().stream()
                .map(itemDto -> {
                    ItemsEntity product = itemsRepository
                            .findById(itemDto.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found with id "
                                    + itemDto.getProductId()));

                    return OrderItemsEntity.builder()
                            .item(product)
                            .quantity(itemDto.getQuantity())
                            .order(orderEntity)
                            .build();
                })
                .collect(Collectors.toList());

        orderEntity.setOrderItems(items);


        BigDecimal totalPrice = items.stream()
                .map(oi -> oi.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderEntity.setTotalPrice(totalPrice);

        ordersRepository.save(orderEntity);

        return orderMapper.toFullOrderDTOFromEntity(orderEntity);
    }

    @Override
    public FullOrderDTO getOrderById(Long id) {

        OrdersEntity ordersEntity = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        return orderMapper.toFullOrderDTOFromEntity(ordersEntity);

    }

    @Override
    public Page<FullOrderDTO> getOrdersFiltered(List<OrderStatus> statuses, LocalDateTime from, LocalDateTime to, int page, int size) {
        Specification<OrdersEntity> spec = Specification
                .where(OrderSpecifications.statusIn(statuses))
                .and(OrderSpecifications.createdAfter(from))
                .and(OrderSpecifications.createdBefore(to));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrdersEntity> all = ordersRepository.findAll(spec, pageable);
        Page<FullOrderDTO> map = all.map(orderMapper::toFullOrderDTOFromEntity);


        return map;
    }

    @Override
    public List<OrdersSummaryDTO> getOrdersByUserId(Long userId) {
        List<OrdersEntity> byUserId = ordersRepository.findByUserId(userId);
        return byUserId.stream()
                .map(orderMapper::toOrdersSummaryDTOFromEntity)
                .toList();
    }

    @Override
    @Transactional
    public FullOrderDTO updateOrder(Long id, OrdersEntity update) {
        FullOrderDTO orderById = getOrderById(id);

        OrdersEntity existing = orderMapper.toOrdersEntityFromFullOrderDTO(orderById);

        existing.setStatus(update.getStatus());
        existing.setTotalPrice(update.getTotalPrice());
        existing.setDeleted(update.isDeleted());
        existing.setOrderItems(update.getOrderItems());

        OrdersEntity save = ordersRepository.save(existing);

        return orderMapper.toFullOrderDTOFromEntity(save);
    }

    @Transactional
    public void softDeleteOrder(Long id) {
        FullOrderDTO orderById = getOrderById(id);
        orderById.setDeleted(true);

        OrdersEntity del = orderMapper.toOrdersEntityFromFullOrderDTO(orderById);

        ordersRepository.save(del);
    }
}
