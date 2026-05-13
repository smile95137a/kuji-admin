package com.group.admin.mapper;

import com.group.admin.entity.CooperationInquiry;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 合作洽談 Mapper
 */
public interface CooperationInquiryMapper {

    /**
     * 選擇性新增合作洽談資料
     *
     * @param row 合作洽談資料
     * @return 影響筆數
     */
    int insertSelective(CooperationInquiry row);

    /**
     * 依主鍵查詢合作洽談資料
     *
     * @param id 主鍵 UUID
     * @return 合作洽談資料
     */
    CooperationInquiry selectByPrimaryKey(String id);

    /**
     * 依 ID 查詢合作洽談資料
     *
     * 給 Service 使用，功能同 selectByPrimaryKey。
     *
     * @param id 合作洽談 ID
     * @return 合作洽談資料
     */
    CooperationInquiry selectById(@Param("id") String id);

    /**
     * 查詢全部未刪除合作洽談資料
     *
     * 注意：此查詢會排除 deleted = 1 的資料。
     *
     * @return 合作洽談資料列表
     */
    List<CooperationInquiry> selectAll();

    /**
     * 依主鍵選擇性更新合作洽談資料
     *
     * @param row 合作洽談更新資料
     * @return 影響筆數
     */
    int updateByPrimaryKeySelective(CooperationInquiry row);

    /**
     * 更新合作洽談處理狀態
     *
     * @param id 合作洽談 ID
     * @param status 處理狀態：PENDING / PROCESSING / DONE / CLOSED
     * @param remark 後台備註
     * @return 影響筆數
     */
    int updateStatus(@Param("id") String id,
                     @Param("status") String status,
                     @Param("remark") String remark);

    /**
     * 合作洽談轉成廠商 AdminUser 後回寫轉換資訊
     *
     * @param id 合作洽談 ID
     * @param vendorAdminUserId 轉成的廠商 AdminUser ID
     * @return 影響筆數
     */
    int updateConvertedVendor(@Param("id") String id,
                              @Param("vendorAdminUserId") String vendorAdminUserId);

    /**
     * 軟刪除合作洽談
     *
     * 不會真的刪除資料，只會標記：
     * deleted = 1
     * deleted_at = 刪除時間
     * deleted_by = 刪除者
     * status = CLOSED
     *
     * @param id 合作洽談 ID
     * @param deletedBy 刪除者 AdminUser ID
     * @param deletedAt 刪除時間
     * @return 影響筆數
     */
    int softDeleteByPrimaryKey(@Param("id") String id,
                               @Param("deletedBy") String deletedBy,
                               @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 依主鍵實體刪除合作洽談資料
     *
     * 注意：原則上不給 Service 使用。
     * 後台刪除請使用 softDeleteByPrimaryKey。
     *
     * @param id 主鍵 UUID
     * @return 影響筆數
     */
    int deleteByPrimaryKey(String id);
}