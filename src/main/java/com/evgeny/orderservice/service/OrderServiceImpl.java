package com.evgeny.orderservice.service;

import com.evgeny.orderservice.dto.order.CreateOrderDTO;
import com.evgeny.orderservice.dto.order.FullOrderDTO;
import com.evgeny.orderservice.dto.order.OrdersSummaryDTO;
import com.evgeny.orderservice.entity.Enums.OrderStatus;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.entity.OrderItemsEntity;
import com.evgeny.orderservice.entity.OrdersEntity;
import com.evgeny.orderservice.exception.InvalidOrderException;
import com.evgeny.orderservice.exception.OrderNotFoundException;
import com.evgeny.orderservice.exception.ProductNotFoundException;
import com.evgeny.orderservice.mapper.order.OrderMapper;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.repository.OrdersRepository;
import com.evgeny.orderservice.specification.OrderSpecifications;
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
import java.util.ArrayList;
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

        if (dto.getOrderItems() == null || dto.getOrderItems().isEmpty()) {
            throw new InvalidOrderException("OrderItems cannot be empty");
        }


        OrdersEntity orderEntity = OrdersEntity.builder()
                .userId(dto.getUserId())
                .status(dto.getStatus())
                .deleted(dto.isDeleted())
                .build();

        List<OrderItemsEntity> items = dto.getOrderItems().stream()
                .map(itemDto -> {
                    ItemsEntity product = itemsRepository
                            .findById(itemDto.getProductId())
                            .orElseThrow(() ->new ProductNotFoundException(itemDto.getProductId()));

                    if (itemDto.getQuantity() <= 0)
                        throw new InvalidOrderException("Quantity must be greater than zero: " + itemDto.getQuantity());

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
                .orElseThrow(() -> new OrderNotFoundException(id));

        return orderMapper.toFullOrderDTOFromEntity(ordersEntity);

    }

    @Override
    public Page<FullOrderDTO> getOrdersFiltered(List<OrderStatus> statuses, LocalDateTime from, LocalDateTime to, int page, int size) {
        Specification<OrdersEntity> spec = Specification

                .where(OrderSpecifications.statusIn(statuses))
                .and(OrderSpecifications.notDeleted())
                .and(OrderSpecifications.createdAfter(from))
                .and(OrderSpecifications.createdBefore(to));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrdersEntity> all = ordersRepository.findAll(spec, pageable);
        return all.map(orderMapper::toFullOrderDTOFromEntity);
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
    public FullOrderDTO updateOrder(Long id, FullOrderDTO update) {

        OrdersEntity ordersEntity = ordersRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + id));

        if (update.getOrderItems() == null || update.getOrderItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        ordersEntity.setStatus(update.getStatus());
        ordersEntity.setDeleted(update.isDeleted());
        ordersEntity.setUpdatedAt(LocalDateTime.now());

        List<OrderItemsEntity> updatedItems = update.getOrderItems().stream()
                .map(dto -> {
                    OrderItemsEntity item = new OrderItemsEntity();

                    item.setId(dto.getId());
                    item.setQuantity(dto.getQuantity());

                    ItemsEntity product = itemsRepository.findById(dto.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(dto.getProductId()));

                    item.setItem(product);
                    item.setOrder(ordersEntity);

                    return item;
                })
                .toList();

        if (ordersEntity.getOrderItems() == null) {
            ordersEntity.setOrderItems(new ArrayList<>());
        } else {
            ordersEntity.getOrderItems().clear();
        }
        ordersEntity.getOrderItems().addAll(updatedItems);

        BigDecimal totalPrice = updatedItems.stream()
                .map(i -> i.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ordersEntity.setTotalPrice(totalPrice);

        OrdersEntity saved = ordersRepository.save(ordersEntity);
        return orderMapper.toFullOrderDTOFromEntity(saved);
    }

    @Transactional
    public void softDeleteOrder(Long id) {

        OrdersEntity order = ordersRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setDeleted(true);

        ordersRepository.save(order);
    }
}
