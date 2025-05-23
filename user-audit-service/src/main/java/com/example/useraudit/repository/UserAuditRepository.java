package com.example.useraudit.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.example.useraudit.model.UserAudit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
public class UserAuditRepository {

    private final CqlSession session;
    private PreparedStatement insertStatement;
    private PreparedStatement findByUserIdStatement;
    private PreparedStatement findByEntityStatement;

    public UserAuditRepository(CqlSession session) {
        this.session = session;
        initStatements();
        createTableIfNotExists();
    }

    private void initStatements() {
        insertStatement = session.prepare(
                "INSERT INTO user_audit (id, user_id, username, action_type, entity_type, entity_id, action_details, ip_address, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        findByUserIdStatement = session.prepare(
                "SELECT * FROM user_audit WHERE user_id = ? ALLOW FILTERING");
        
        findByEntityStatement = session.prepare(
                "SELECT * FROM user_audit WHERE entity_type = ? AND entity_id = ? ALLOW FILTERING");
    }

    private void createTableIfNotExists() {
        session.execute(
                "CREATE TABLE IF NOT EXISTS user_audit (" +
                "id uuid PRIMARY KEY, " +
                "user_id bigint, " +
                "username text, " +
                "action_type text, " +
                "entity_type text, " +
                "entity_id text, " +
                "action_details text, " +
                "ip_address text, " +
                "timestamp timestamp" +
                ") WITH default_time_to_live = 31536000");
        
        log.info("Таблица user_audit создана или уже существует");
    }

    public UserAudit save(UserAudit audit) {
        if (audit.getId() == null) {
            audit.setId(UUID.randomUUID());
        }
        
        if (audit.getTimestamp() == null) {
            audit.setTimestamp(Instant.now());
        }
        
        BoundStatement bound = insertStatement.bind()
                .setUuid(0, audit.getId())
                .setLong(1, audit.getUserId())
                .setString(2, audit.getUsername())
                .setString(3, audit.getActionType())
                .setString(4, audit.getEntityType())
                .setString(5, audit.getEntityId())
                .setString(6, audit.getActionDetails())
                .setString(7, audit.getIpAddress())
                .setInstant(8, audit.getTimestamp());
        
        session.execute(bound);
        log.debug("Сохранена запись аудита: {}", audit);
        
        return audit;
    }

    public List<UserAudit> findByUserId(Long userId) {
        BoundStatement bound = findByUserIdStatement.bind().setLong(0, userId);
        ResultSet resultSet = session.execute(bound);
        
        List<UserAudit> auditList = new ArrayList<>();
        for (Row row : resultSet) {
            auditList.add(mapRowToUserAudit(row));
        }
        
        return auditList;
    }

    public List<UserAudit> findByEntity(String entityType, String entityId) {
        BoundStatement bound = findByEntityStatement.bind()
                .setString(0, entityType)
                .setString(1, entityId);
        
        ResultSet resultSet = session.execute(bound);
        
        List<UserAudit> auditList = new ArrayList<>();
        for (Row row : resultSet) {
            auditList.add(mapRowToUserAudit(row));
        }
        
        return auditList;
    }

    private UserAudit mapRowToUserAudit(Row row) {
        return UserAudit.builder()
                .id(row.getUuid("id"))
                .userId(row.getLong("user_id"))
                .username(row.getString("username"))
                .actionType(row.getString("action_type"))
                .entityType(row.getString("entity_type"))
                .entityId(row.getString("entity_id"))
                .actionDetails(row.getString("action_details"))
                .ipAddress(row.getString("ip_address"))
                .timestamp(row.getInstant("timestamp"))
                .build();
    }
} 