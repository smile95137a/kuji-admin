package com.group.admin.repository;

import com.group.admin.condition.BusinessEventLogCondition;
import com.group.admin.entity.BusinessEventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BusinessEventLogRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<BusinessEventLog> ROW_MAPPER = (rs, rowNum) -> BusinessEventLog.builder()
            .id(rs.getString("id"))
            .eventType(rs.getString("event_type"))
            .action(rs.getString("action"))
            .result(rs.getString("result"))
            .actorType(rs.getString("actor_type"))
            .actorId(rs.getString("actor_id"))
            .actorName(rs.getString("actor_name"))
            .targetType(rs.getString("target_type"))
            .targetId(rs.getString("target_id"))
            .targetNo(rs.getString("target_no"))
            .userId(rs.getString("user_id"))
            .orderId(rs.getString("order_id"))
            .rechargeId(rs.getString("recharge_id"))
            .walletTransactionId(rs.getString("wallet_transaction_id"))
            .externalProvider(rs.getString("external_provider"))
            .externalRef(rs.getString("external_ref"))
            .amount(readLong(rs, "amount"))
            .paymentMethod(rs.getString("payment_method"))
            .beforeStatus(rs.getString("before_status"))
            .afterStatus(rs.getString("after_status"))
            .beforeSnapshot(rs.getString("before_snapshot"))
            .afterSnapshot(rs.getString("after_snapshot"))
            .callbackSummary(rs.getString("callback_summary"))
            .rawPayloadHash(rs.getString("raw_payload_hash"))
            .errorMessage(rs.getString("error_message"))
            .ip(rs.getString("ip"))
            .userAgent(rs.getString("user_agent"))
            .createdAt(readLocalDateTime(rs, "created_at"))
            .build();

    public void insert(BusinessEventLog log) {
        jdbcTemplate.update("""
                INSERT INTO log_business_event (
                    id, event_type, action, result, actor_type, actor_id, actor_name,
                    target_type, target_id, target_no, user_id, order_id, recharge_id,
                    wallet_transaction_id, external_provider, external_ref, amount, payment_method,
                    before_status, after_status, before_snapshot, after_snapshot,
                    callback_summary, raw_payload_hash, error_message, ip, user_agent, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                log.getId(),
                log.getEventType(),
                log.getAction(),
                log.getResult(),
                log.getActorType(),
                log.getActorId(),
                log.getActorName(),
                log.getTargetType(),
                log.getTargetId(),
                log.getTargetNo(),
                log.getUserId(),
                log.getOrderId(),
                log.getRechargeId(),
                log.getWalletTransactionId(),
                log.getExternalProvider(),
                log.getExternalRef(),
                log.getAmount(),
                log.getPaymentMethod(),
                log.getBeforeStatus(),
                log.getAfterStatus(),
                log.getBeforeSnapshot(),
                log.getAfterSnapshot(),
                log.getCallbackSummary(),
                log.getRawPayloadHash(),
                log.getErrorMessage(),
                log.getIp(),
                log.getUserAgent(),
                log.getCreatedAt());
    }

    public List<BusinessEventLog> findByCondition(BusinessEventLogCondition condition, int limit) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM log_business_event WHERE 1=1");

        appendEquals(sql, args, "event_type", condition != null ? condition.getEventType() : null);
        appendEquals(sql, args, "result", condition != null ? condition.getResult() : null);
        appendEquals(sql, args, "action", condition != null ? condition.getAction() : null);
        appendEquals(sql, args, "actor_id", condition != null ? condition.getActorId() : null);
        appendEquals(sql, args, "user_id", condition != null ? condition.getUserId() : null);
        appendEquals(sql, args, "order_id", condition != null ? condition.getOrderId() : null);
        appendEquals(sql, args, "recharge_id", condition != null ? condition.getRechargeId() : null);
        appendEquals(sql, args, "target_id", condition != null ? condition.getTargetId() : null);
        appendLike(sql, args, "external_ref", condition != null ? condition.getExternalRef() : null);

        if (condition != null && condition.getStartAt() != null) {
            sql.append(" AND created_at >= ?");
            args.add(condition.getStartAt());
        }
        if (condition != null && condition.getEndAt() != null) {
            sql.append(" AND created_at <= ?");
            args.add(condition.getEndAt());
        }

        sql.append(" ORDER BY created_at DESC LIMIT ?");
        args.add(Math.max(1, Math.min(limit, 1000)));
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    public int deleteBefore(LocalDateTime threshold) {
        return jdbcTemplate.update("DELETE FROM log_business_event WHERE created_at < ?", threshold);
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        sql.append(" AND ").append(column).append(" = ?");
        args.add(value.trim());
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        sql.append(" AND ").append(column).append(" LIKE ?");
        args.add("%" + value.trim() + "%");
    }

    private static Long readLong(ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime readLocalDateTime(ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value != null ? value.toLocalDateTime() : null;
    }
}
