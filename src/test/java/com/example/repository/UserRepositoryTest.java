package com.example.repository;

import com.example.model.Order;
import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldFindByEmail() {
        Optional<User> found = userRepository.findByEmail(testUser.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
        assertThat(found.get().getName()).isEqualTo(testUser.getName());
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void shouldExistsByEmail() {
        boolean exists = userRepository.existsByEmail(testUser.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldNotExistByWrongEmail() {
        boolean exists = userRepository.existsByEmail("wrong@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindByNameContainingIgnoreCase() {
        User secondUser = User.builder()
                .name("Петр Петров")
                .email("petr@example.com")
                .build();
        userRepository.save(secondUser);

        List<User> users = userRepository.findByNameContainingIgnoreCase("иван");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindByEmailDomain() {
        User secondUser = User.builder()
                .name("Анна Смирнова")
                .email("anna@gmail.com")
                .build();
        userRepository.save(secondUser);

        List<User> users = userRepository.findByEmailDomain("example.com");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindByIdWithOrders() {
        Order order = Order.builder()
                .orderNumber("ORD-001")
                .totalAmount(new BigDecimal("100.00"))
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();
        orderRepository.save(order);
        
        entityManager.flush();
        entityManager.clear();

        Optional<User> userWithOrders = userRepository.findByIdWithOrders(testUser.getId());

        assertThat(userWithOrders).isPresent();
        assertThat(userWithOrders.get().getOrders()).isNotNull();
        if (userWithOrders.get().getOrders().isEmpty()) {
            System.out.println("ВНИМАНИЕ: Список заказов пуст! ID пользователя: " + testUser.getId());
            
            List<Order> allOrders = orderRepository.findAll();
            System.out.println("Всего заказов в БД: " + allOrders.size());
            for (Order o : allOrders) {
                System.out.println("Заказ: ID=" + o.getId() + ", номер=" + o.getOrderNumber() + 
                                  ", пользователь ID=" + (o.getUser() != null ? o.getUser().getId() : "null"));
            }
        }
        
        assertThat(userWithOrders.get().getOrders()).hasSize(1);
        assertThat(userWithOrders.get().getOrders().get(0).getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    void shouldCheckIfUserHasOrders() {
        Order order = Order.builder()
                .orderNumber("ORD-001")
                .totalAmount(new BigDecimal("100.00"))
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();
        orderRepository.save(order);

        boolean hasOrders = userRepository.hasOrders(testUser.getId());

        assertThat(hasOrders).isTrue();
        
        User secondUser = User.builder()
                .name("Без заказов")
                .email("no-orders@example.com")
                .build();
        secondUser = userRepository.save(secondUser);
        
        assertThat(userRepository.hasOrders(secondUser.getId())).isFalse();
    }
} 