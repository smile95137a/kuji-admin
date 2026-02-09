package com.group.admin.repository;

import com.group.admin.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Order 自定義查詢 Repository
 * 與 MBG 生成的 OrderMapper 分離，避免每次重新生成時被覆蓋
 */
@Mapper
public interface OrderRepository {

    /**
     * 根據使用者 ID 查詢所有訂單
     */
    @Select("SELECT id, order_no AS orderNo, user_id AS userId, store_id AS storeId, " +
            "total_items AS totalItems, shipping_method AS shippingMethod, " +
            "shipping_status AS shippingStatus, recipient_name AS recipientName, " +
            "recipient_phone AS recipientPhone, recipient_address AS recipientAddress, " +
            "store_code AS storeCode, store_name AS storeName, store_address AS storeAddress, " +
            "tracking_no AS trackingNo, remark, created_at AS createdAt, updated_at AS updatedAt, " +
            "shipped_at AS shippedAt, completed_at AS completedAt, cancelled_at AS cancelledAt, " +
            "cancelled_by AS cancelledBy, cancel_reason AS cancelReason " +
            "FROM `order` WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> selectByUserId(String userId);

    /**
     * 查詢所有訂單
     */
    @Select("SELECT id, order_no AS orderNo, user_id AS userId, store_id AS storeId, " +
            "total_items AS totalItems, shipping_method AS shippingMethod, " +
            "shipping_status AS shippingStatus, recipient_name AS recipientName, " +
            "recipient_phone AS recipientPhone, recipient_address AS recipientAddress, " +
            "store_code AS storeCode, store_name AS storeName, store_address AS storeAddress, " +
            "tracking_no AS trackingNo, remark, created_at AS createdAt, updated_at AS updatedAt, " +
            "shipped_at AS shippedAt, completed_at AS completedAt, cancelled_at AS cancelledAt, " +
            "cancelled_by AS cancelledBy, cancel_reason AS cancelReason " +
            "FROM `order` ORDER BY created_at DESC")
    List<Order> selectAll();
}
