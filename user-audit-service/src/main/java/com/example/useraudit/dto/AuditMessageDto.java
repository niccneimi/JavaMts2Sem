package com.example.useraudit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditMessageDto {
    private Long userId;
    private String username;
    private String actionType;
    private String entityType;
    private String entityId;
    private String actionDetails;
    private String ipAddress;
    private Instant timestamp;
} 