package com.example.repository;

import com.example.model.Order;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {
    List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findComplexOrders(List<String> statuses, Long userId, boolean sortByAmount);
} 