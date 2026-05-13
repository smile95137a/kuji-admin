package com.group.admin.mapper;

import com.group.admin.entity.CooperationInquiryStatusLog;

import java.util.List;

/**
 * 合作洽詢狀態異動紀錄 Mapper
 */
public interface CooperationInquiryStatusLogMapper {

    /**
     * 新增合作洽詢狀態異動紀錄
     *
     * @param row 合作洽詢狀態異動紀錄
     * @return 影響筆數
     */
    int insertSelective(CooperationInquiryStatusLog row);

    /**
     * 依主鍵查詢合作洽詢狀態異動紀錄
     *
     * @param id 主鍵 UUID
     * @return 合作洽詢狀態異動紀錄
     */
    CooperationInquiryStatusLog selectByPrimaryKey(String id);

    /**
     * 依合作洽詢 ID 查詢狀態異動紀錄
     *
     * @param inquiryId 合作洽詢 ID
     * @return 狀態異動紀錄列表
     */
    List<CooperationInquiryStatusLog> selectByInquiryId(String inquiryId);

    /**
     * 依主鍵刪除合作洽詢狀態異動紀錄
     *
     * @param id 主鍵 UUID
     * @return 影響筆數
     */
    int deleteByPrimaryKey(String id);
}