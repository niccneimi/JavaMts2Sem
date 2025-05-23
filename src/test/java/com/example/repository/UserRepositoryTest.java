package com.example.repository;

import com.example.model.Order;
import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

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

        Optional<User> userWithOrders = userRepository.findByIdWithOrders(testUser.getId());

        assertThat(userWithOrders).isPresent();
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