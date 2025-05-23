package com.example.service;

import com.example.dto.OrderDto;
import com.example.model.Order;
import com.example.model.User;
import com.example.repository.OrderRepository;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "orders")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<OrderDto> getAllOrders() {
        log.debug("Получение всех заказов");
        return orderRepository.findAll().stream()
            .map(OrderDto::new)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "ordersByPage")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<OrderDto> getOrdersPaged(Pageable pageable) {
        log.debug("Получение заказов с пагинацией");
        return orderRepository.findAll(pageable)
            .map(OrderDto::new);
    }

    @Cacheable(value = "orderById", key = "#id")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public OrderDto getOrderById(Long id) {
        log.debug("Получение заказа по ID: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден с ID: " + id));
        return new OrderDto(order);
    }

    @Cacheable(value = "orderWithUserById", key = "#id")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public OrderDto getOrderWithUser(Long id) {
        log.debug("Получение заказа с данными пользователя по ID: {}", id);
        Order order = orderRepository.findByIdWithUser(id);
        if (order == null) {
            throw new RuntimeException("Заказ не найден с ID: " + id);
        }
        return new OrderDto(order);
    }

    @Cacheable(value = "ordersByUser", key = "#userId")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<OrderDto> getOrdersByUserId(Long userId) {
        log.debug("Получение заказов по ID пользователя: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));
        return orderRepository.findByUser(user).stream()
            .map(OrderDto::new)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "ordersByStatus", key = "#status")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<OrderDto> getOrdersByStatus(String status) {
        log.debug("Получение заказов по статусу: {}", status);
        return orderRepository.findByStatus(status).stream()
            .map(OrderDto::new)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "ordersByUserAndStatus", key = "#userId + '-' + #status")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<OrderDto> getOrdersByUserIdAndStatus(Long userId, String status) {
        log.debug("Получение заказов по ID пользователя: {} и статусу: {}", userId, status);
        return orderRepository.findByUserIdAndStatus(userId, status).stream()
            .map(OrderDto::new)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "ordersByAmountGreaterThan", key = "#amount")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<OrderDto> getOrdersByAmountGreaterThan(BigDecimal amount) {
        log.debug("Получение заказов с суммой больше: {}", amount);
        return orderRepository.findByTotalAmountGreaterThan(amount).stream()
            .map(OrderDto::new)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "ordersByDateRange")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<OrderDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Получение заказов в диапазоне дат от {} до {}", startDate, endDate);
        return orderRepository.findOrdersByDateRange(startDate, endDate).stream()
            .map(OrderDto::new)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "complexOrders")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<OrderDto> getComplexOrders(List<String> statuses, Long userId, boolean sortByAmount) {
        log.debug("Получение заказов по сложному запросу");
        return orderRepository.findComplexOrders(statuses, userId, sortByAmount).stream()
            .map(OrderDto::new)
            .collect(Collectors.toList());
    }

    @Caching(
        put = {
            @CachePut(value = "orderById", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "orders", allEntries = true),
            @CacheEvict(value = "ordersByPage", allEntries = true),
            @CacheEvict(value = "ordersByUser", key = "#order.user.id", condition = "#order.user != null")
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public OrderDto createOrder(Order order) {
        log.debug("Создание нового заказа");
        
        if (order.getUser() != null && order.getUser().getId() != null) {
            User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + order.getUser().getId()));
            order.setUser(user);
        }
        
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        
        Order savedOrder = orderRepository.save(order);
        return new OrderDto(savedOrder);
    }

    @Caching(
        put = {
            @CachePut(value = "orderById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "orders", allEntries = true),
            @CacheEvict(value = "ordersByPage", allEntries = true),
            @CacheEvict(value = "ordersByUser", allEntries = true),
            @CacheEvict(value = "ordersByStatus", allEntries = true),
            @CacheEvict(value = "ordersByUserAndStatus", allEntries = true),
            @CacheEvict(value = "ordersByAmountGreaterThan", allEntries = true),
            @CacheEvict(value = "ordersByDateRange", allEntries = true),
            @CacheEvict(value = "complexOrders", allEntries = true),
            @CacheEvict(value = "orderWithUserById", key = "#id")
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public OrderDto updateOrder(Long id, Order orderDetails) {
        log.debug("Обновление заказа с ID: {}", id);
        Order existingOrder = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден с ID: " + id));

        if (orderDetails.getOrderNumber() != null) {
            existingOrder.setOrderNumber(orderDetails.getOrderNumber());
        }
        
        if (orderDetails.getTotalAmount() != null) {
            existingOrder.setTotalAmount(orderDetails.getTotalAmount());
        }
        
        if (orderDetails.getStatus() != null) {
            existingOrder.setStatus(orderDetails.getStatus());
        }
        
        if (orderDetails.getUser() != null && orderDetails.getUser().getId() != null) {
            User user = userRepository.findById(orderDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + orderDetails.getUser().getId()));
            existingOrder.setUser(user);
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        return new OrderDto(updatedOrder);
    }

    @Caching(
        evict = {
            @CacheEvict(value = "orderById", key = "#id"),
            @CacheEvict(value = "orders", allEntries = true),
            @CacheEvict(value = "ordersByPage", allEntries = true),
            @CacheEvict(value = "ordersByUser", allEntries = true),
            @CacheEvict(value = "ordersByStatus", allEntries = true),
            @CacheEvict(value = "ordersByUserAndStatus", allEntries = true),
            @CacheEvict(value = "ordersByAmountGreaterThan", allEntries = true),
            @CacheEvict(value = "ordersByDateRange", allEntries = true),
            @CacheEvict(value = "complexOrders", allEntries = true),
            @CacheEvict(value = "orderWithUserById", key = "#id")
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void deleteOrder(Long id) {
        log.debug("Удаление заказа с ID: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден с ID: " + id));
        orderRepository.delete(order);
    }

    @Caching(
        evict = {
            @CacheEvict(value = "orderById", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true),
            @CacheEvict(value = "ordersByPage", allEntries = true),
            @CacheEvict(value = "ordersByUser", key = "#userId"),
            @CacheEvict(value = "ordersByStatus", allEntries = true),
            @CacheEvict(value = "ordersByUserAndStatus", allEntries = true),
            @CacheEvict(value = "ordersByAmountGreaterThan", allEntries = true),
            @CacheEvict(value = "ordersByDateRange", allEntries = true),
            @CacheEvict(value = "complexOrders", allEntries = true),
            @CacheEvict(value = "orderWithUserById", allEntries = true)
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int deleteOrdersByUserId(Long userId) {
        log.debug("Удаление всех заказов пользователя с ID: {}", userId);
        return orderRepository.deleteAllByUserId(userId);
    }

    @Caching(
        put = {
            @CachePut(value = "orderById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "orders", allEntries = true),
            @CacheEvict(value = "ordersByStatus", allEntries = true),
            @CacheEvict(value = "ordersByUserAndStatus", allEntries = true),
            @CacheEvict(value = "complexOrders", allEntries = true),
            @CacheEvict(value = "orderWithUserById", key = "#id")
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public OrderDto updateOrderStatus(Long id, String status) {
        log.debug("Обновление статуса заказа с ID: {} на: {}", id, status);
        int updatedRows = orderRepository.updateStatus(id, status);
        if (updatedRows == 0) {
            throw new RuntimeException("Не удалось обновить статус заказа с ID: " + id);
        }
        return getOrderById(id);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public long countOrdersByUserId(Long userId) {
        log.debug("Подсчет количества заказов пользователя с ID: {}", userId);
        return orderRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public BigDecimal sumOrderAmountsByUserId(Long userId) {
        log.debug("Подсчет суммы заказов пользователя с ID: {}", userId);
        BigDecimal sum = orderRepository.sumTotalAmountByUserId(userId);
        return sum != null ? sum : BigDecimal.ZERO;
    }
} 