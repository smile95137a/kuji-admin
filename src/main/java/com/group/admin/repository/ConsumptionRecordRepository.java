package com.group.admin.repository;

import com.group.admin.entity.ConsumptionRecord;
import com.group.admin.req.consumption.ConsumptionRecordCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消費紀錄自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface ConsumptionRecordRepository {

    @Select("SELECT * FROM consumption_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ConsumptionRecord> selectByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM consumption_record ORDER BY created_at DESC")
    List<ConsumptionRecord> selectAll();

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM consumption_record
            <where>
              <if test="condition != null">
                <if test="condition.userId != null and condition.userId != ''">
                  AND user_id = #{condition.userId}
                </if>
                <if test="condition.type != null and condition.type != ''">
                  AND type = #{condition.type}
                </if>
                <if test="condition.lotteryId != null and condition.lotteryId != ''">
                  AND lottery_id = #{condition.lotteryId}
                </if>
                <if test="condition.orderNumber != null and condition.orderNumber != ''">
                  AND order_number LIKE CONCAT('%', #{condition.orderNumber}, '%')
                </if>
                <if test="condition.keyword != null and condition.keyword != ''">
                  AND (
                    lottery_title LIKE CONCAT('%', #{condition.keyword}, '%')
                    OR order_number LIKE CONCAT('%', #{condition.keyword}, '%')
                    OR description LIKE CONCAT('%', #{condition.keyword}, '%')
                  )
                </if>
                <if test="condition.createdAtStart != null">
                  AND created_at &gt;= #{condition.createdAtStart}
                </if>
                <if test="condition.createdAtEnd != null">
                  AND created_at &lt; DATE_ADD(#{condition.createdAtEnd}, INTERVAL 1 DAY)
                </if>
              </if>
            </where>
            </script>
            """)
    long countByCondition(@Param("condition") ConsumptionRecordCondition condition);

    @Select("""
            <script>
            SELECT id, user_id, type, lottery_id, lottery_title, order_id, order_number,
                   gold_amount, bonus_amount, description, created_at
            FROM consumption_record
            <where>
              <if test="condition != null">
                <if test="condition.userId != null and condition.userId != ''">
                  AND user_id = #{condition.userId}
                </if>
                <if test="condition.type != null and condition.type != ''">
                  AND type = #{condition.type}
                </if>
                <if test="condition.lotteryId != null and condition.lotteryId != ''">
                  AND lottery_id = #{condition.lotteryId}
                </if>
                <if test="condition.orderNumber != null and condition.orderNumber != ''">
                  AND order_number LIKE CONCAT('%', #{condition.orderNumber}, '%')
                </if>
                <if test="condition.keyword != null and condition.keyword != ''">
                  AND (
                    lottery_title LIKE CONCAT('%', #{condition.keyword}, '%')
                    OR order_number LIKE CONCAT('%', #{condition.keyword}, '%')
                    OR description LIKE CONCAT('%', #{condition.keyword}, '%')
                  )
                </if>
                <if test="condition.createdAtStart != null">
                  AND created_at &gt;= #{condition.createdAtStart}
                </if>
                <if test="condition.createdAtEnd != null">
                  AND created_at &lt; DATE_ADD(#{condition.createdAtEnd}, INTERVAL 1 DAY)
                </if>
              </if>
            </where>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<ConsumptionRecord> selectByConditionPaged(@Param("condition") ConsumptionRecordCondition condition,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);
}
