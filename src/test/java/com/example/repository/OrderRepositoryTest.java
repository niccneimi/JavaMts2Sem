package com.example.repository;

import com.example.model.Order;
import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        testUser = User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        testUser = userRepository.save(testUser);

        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .totalAmount(new BigDecimal("100.00"))
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();

        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void shouldFindByUser() {
        List<Order> orders = orderRepository.findByUser(testUser);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void shouldFindByStatus() {
        List<Order> orders = orderRepository.findByStatus("NEW");

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void shouldFindByTotalAmountGreaterThan() {
        List<Order> orders = orderRepository.findByTotalAmountGreaterThan(new BigDecimal("50.00"));

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void shouldNotFindByTotalAmountGreaterThan() {
        List<Order> orders = orderRepository.findByTotalAmountGreaterThan(new BigDecimal("150.00"));

        assertThat(orders).isEmpty();
    }

    @Test
    void shouldFindByUserIdAndStatus() {
        List<Order> orders = orderRepository.findByUserIdAndStatus(testUser.getId(), "NEW");

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void shouldFindByIdWithUser() {
        Order order = orderRepository.findByIdWithUser(testOrder.getId());

        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo(testOrder.getId());
        assertThat(order.getUser()).isNotNull();
        assertThat(order.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldCountByUserId() {
        long count = orderRepository.countByUserId(testUser.getId());

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void shouldSumTotalAmountByUserId() {
        Order secondOrder = Order.builder()
                .orderNumber("ORD-002")
                .totalAmount(new BigDecimal("200.00"))
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();

        orderRepository.save(secondOrder);

        BigDecimal sum = orderRepository.sumTotalAmountByUserId(testUser.getId());

        assertThat(sum).isEqualTo(new BigDecimal("300.00"));
    }

    @Test
    void shouldUpdateStatus() {
        int updatedRows = orderRepository.updateStatus(testOrder.getId(), "COMPLETED");

        assertThat(updatedRows).isEqualTo(1);
        
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElse(null);
        
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void shouldDeleteAllByUserId() {
        int deletedRows = orderRepository.deleteAllByUserId(testUser.getId());

        assertThat(deletedRows).isEqualTo(1);
        
        List<Order> remainingOrders = orderRepository.findAll();
        
        assertThat(remainingOrders).isEmpty();
    }

    @Test
    void shouldFindOrdersByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void shouldFindComplexOrders() {
        Order secondOrder = Order.builder()
                .orderNumber("ORD-002")
                .totalAmount(new BigDecimal("200.00"))
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();

        orderRepository.save(secondOrder);
        
        List<Order> newOrders = orderRepository.findComplexOrders(Arrays.asList("NEW"), null, false);
        
        assertThat(newOrders).hasSize(1);
        assertThat(newOrders.get(0).getStatus()).isEqualTo("NEW");
        
        List<Order> userOrders = orderRepository.findComplexOrders(null, testUser.getId(), true);
        
        assertThat(userOrders).hasSize(2);
        assertThat(userOrders.get(0).getTotalAmount()).isGreaterThan(userOrders.get(1).getTotalAmount());
    }
} 