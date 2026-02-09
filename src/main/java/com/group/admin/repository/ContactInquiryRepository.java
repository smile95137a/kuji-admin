package com.group.admin.repository;

import com.group.admin.entity.ContactInquiry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 合作諮詢自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface ContactInquiryRepository {

    @Select("SELECT * FROM contact_inquiry ORDER BY created_at DESC")
    List<ContactInquiry> selectAll();

    @Update("UPDATE contact_inquiry SET status = #{status}, remark = #{remark}, " +
            "processed_by = #{processedBy}, processed_at = #{processedAt}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(ContactInquiry inquiry);
}
