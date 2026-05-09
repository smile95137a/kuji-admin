package com.group.admin.mapper;

import java.util.List;

import com.group.admin.entity.CooperationInquiry;

public interface CooperationInquiryMapper {

    int insertSelective(CooperationInquiry row);

    CooperationInquiry selectByPrimaryKey(String id);

    List<CooperationInquiry> selectAll();

    int updateByPrimaryKeySelective(CooperationInquiry row);

    int deleteByPrimaryKey(String id);
}