package com.example.service;

import com.example.model.Order;
import com.example.model.User;
import com.example.repository.OrderRepository;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    
    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }
    
    public List<Order> getAllOrders() {
        log.debug("Получение всех заказов");
        return orderRepository.findAll();
    }
    
    public Order getOrderById(Long id) {
        log.debug("Получение заказа по ID: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }
    
    public List<Order> getOrdersByUser(Long userId) {
        log.debug("Получение заказов пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return orderRepository.findByUser(user);
    }
    
    public List<Order> getOrdersByStatus(String status) {
        log.debug("Получение заказов со статусом: {}", status);
        return orderRepository.findByStatus(status);
    }
    
    public List<Order> getOrdersWithAmountGreaterThan(BigDecimal amount) {
        log.debug("Получение заказов с суммой больше: {}", amount);
        return orderRepository.findByTotalAmountGreaterThan(amount);
    }
    
    @Transactional
    public Order createOrder(Order order, Long userId) {
        log.debug("Создание заказа для пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        order.setUser(user);
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        log.debug("Обновление статуса заказа с ID: {} на: {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    @Transactional
    public void deleteOrder(Long id) {
        log.debug("Удаление заказа с ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        orderRepository.delete(order);
    }
} 