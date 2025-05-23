package com.example.useraudit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAudit {
    
    private UUID id;
    
    private Long userId;
    
    private String username;
    
    private String actionType;

    private String entityType;
    
    private String entityId;
    
    private String actionDetails;
    
    private String ipAddress;
    
    private Instant timestamp;
} 