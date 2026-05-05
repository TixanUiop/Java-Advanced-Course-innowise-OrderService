package com.evgeny.orderservice.service;

import com.evgeny.orderservice.dto.orderItems.CreateOrderItemDTO;
import com.evgeny.orderservice.dto.orderItems.FullOrderItemDTO;
import com.evgeny.orderservice.entity.ItemsEntity;
import com.evgeny.orderservice.entity.OrderItemsEntity;
import com.evgeny.orderservice.entity.OrdersEntity;
import com.evgeny.orderservice.exception.*;
import com.evgeny.orderservice.mapper.orderItems.OrderItemMapper;
import com.evgeny.orderservice.repository.ItemsRepository;
import com.evgeny.orderservice.repository.OrderItemsRepository;
import com.evgeny.orderservice.repository.OrdersRepository;
import com.evgeny.orderservice.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemsServiceImpl implements OrderItemsService {

    private final OrderItemsRepository orderItemsRepository;
    private final OrdersRepository ordersRepository;
    private final ItemsRepository itemsRepository;
    private final OrderItemMapper orderItemMapper;


    @Override
    public FullOrderItemDTO getById(Long id) {
        OrderItemsEntity entity = orderItemsRepository.findById(id)
                .orElseThrow(() -> new OrderItemsNotFoundException("OrderItem not found: " + id));

        return orderItemMapper.toDto(entity);
    }

    @Override
    @Transactional
    public FullOrderItemDTO create(CreateOrderItemDTO dto) {


        OrdersEntity orderEntity = ordersRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(dto.getOrderId()));

        JwtUserDetails user = (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long currentUserId = user.getId();

        if (!orderEntity.getUserId().equals(currentUserId)) {
            throw new InvalidOrderOperationException("You cannot add items to someone else's order");
        }

        ItemsEntity itemEntity = itemsRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ProductNotFoundException(dto.getItemId()));


        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new InvalidOrderItemsException("Quantity must be greater than zero");
        }

        if (itemEntity.getPrice() == null || itemEntity.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderItemsException("Invalid product price for item: " + itemEntity.getId());
        }

        OrderItemsEntity orderItem = OrderItemsEntity.builder()
                .order(orderEntity)
                .item(itemEntity)
                .quantity(dto.getQuantity())
                .deleted(false)
                .build();

        orderItemsRepository.save(orderItem);

        return orderItemMapper.toDto(orderItem);
    }

    @Override
    public List<FullOrderItemDTO> getAll() {
        return orderItemsRepository.findAll().stream()
                .filter(e -> !e.getOrder().isDeleted())
                .map(orderItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public FullOrderItemDTO update(Long id, FullOrderItemDTO dto) {

        OrderItemsEntity entity = orderItemsRepository.findById(id)
                .orElseThrow(() -> new OrderItemsNotFoundException("OrderItem not found: " + id));

        entity.setQuantity(dto.getQuantity());

        JwtUserDetails user = (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long currentUserId = user.getId();
        String role = user.getRole();

        if (!entity.getOrder().getUserId().equals(currentUserId) && !role.equals("ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }

        if (dto.getOrderId() != null) {
            OrdersEntity orderEntity = ordersRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + dto.getOrderId()));
            entity.setOrder(orderEntity);
        }

        if (dto.getItemId() != null) {
            ItemsEntity itemEntity = itemsRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new ProductNotFoundException("Item not found: " + dto.getItemId()));
            entity.setItem(itemEntity);
        }

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new InvalidOrderItemsException("Quantity must be greater than zero");
        }

        orderItemsRepository.save(entity);
        return orderItemMapper.toDto(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        OrderItemsEntity entity = orderItemsRepository.findById(id)
                .orElseThrow(() -> new OrderItemsNotFoundException("OrderItem not found: " + id));

        OrdersEntity order = entity.getOrder();

        JwtUserDetails user = (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long currentUserId = user.getId();
        String role = user.getRole();

        boolean isAdmin = role.equals("ADMIN");
        boolean isOwner = order.getUserId().equals(currentUserId);

        if (!isAdmin && !isOwner) {
            throw new InvalidOrderOperationException("Access denied");
        }

        orderItemsRepository.delete(entity);
    }
}
