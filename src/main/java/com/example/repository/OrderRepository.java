package com.example.repository;

import com.example.model.Order;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    List<Order> findByUser(User user);
    
    List<Order> findByStatus(String status);
    
    List<Order> findByTotalAmountGreaterThan(BigDecimal amount);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    Order findByIdWithUser(@Param("id") Long id);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.id = :userId")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus WHERE o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("newStatus") String newStatus);
    
    @Modifying
    @Query("DELETE FROM Order o WHERE o.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
} 