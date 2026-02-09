package com.group.admin.mapper;

import com.group.admin.entity.ContactInquiry;
import com.group.admin.example.ContactInquiryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ContactInquiryMapper {
    long countByExample(ContactInquiryExample example);

    int deleteByExample(ContactInquiryExample example);

    int deleteByPrimaryKey(String id);

    int insert(ContactInquiry row);

    int insertSelective(ContactInquiry row);

    List<ContactInquiry> selectByExampleWithBLOBs(ContactInquiryExample example);

    List<ContactInquiry> selectByExample(ContactInquiryExample example);

    ContactInquiry selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ContactInquiry row, @Param("example") ContactInquiryExample example);

    int updateByExampleWithBLOBs(@Param("row") ContactInquiry row, @Param("example") ContactInquiryExample example);

    int updateByExample(@Param("row") ContactInquiry row, @Param("example") ContactInquiryExample example);

    int updateByPrimaryKeySelective(ContactInquiry row);

    int updateByPrimaryKeyWithBLOBs(ContactInquiry row);

    int updateByPrimaryKey(ContactInquiry row);
}