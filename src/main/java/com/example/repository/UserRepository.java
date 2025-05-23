package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :nameFragment, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("nameFragment") String nameFragment);
    
    @Query(value = "SELECT * FROM users WHERE email LIKE CONCAT('%', :domain, '%')", nativeQuery = true)
    List<User> findByEmailDomain(@Param("domain") String domain);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.user.id = :userId")
    boolean hasOrders(@Param("userId") Long userId);
}