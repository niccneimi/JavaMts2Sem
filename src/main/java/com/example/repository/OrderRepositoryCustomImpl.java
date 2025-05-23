package com.example.repository;

import com.example.model.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate",
                Order.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    @Override
    public List<Order> findComplexOrders(List<String> statuses, Long userId, boolean sortByAmount) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> order = cq.from(Order.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (statuses != null && !statuses.isEmpty()) {
            predicates.add(order.get("status").in(statuses));
        }
        
        if (userId != null) {
            predicates.add(cb.equal(order.get("user").get("id"), userId));
        }
        
        cq.where(predicates.toArray(new Predicate[0]));
        
        if (sortByAmount) {
            cq.orderBy(cb.desc(order.get("totalAmount")));
        } else {
            cq.orderBy(cb.desc(order.get("createdAt")));
        }
        
        return entityManager.createQuery(cq).getResultList();
    }
} 