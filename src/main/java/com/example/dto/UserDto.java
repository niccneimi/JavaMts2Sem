package com.example.dto;

import com.example.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private List<OrderDto> orders;
    
    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        
        if (user.getOrders() != null) {
            this.orders = user.getOrders().stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
        }
    }
} 