package com.group.admin.mapper;

import com.group.admin.entity.VerificationToken;
import com.group.admin.example.VerificationTokenExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VerificationTokenMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(VerificationToken row);

    VerificationToken selectByPrimaryKey(@Param("id") Long id);

    List<VerificationToken> selectAll();

    int updateByPrimaryKey(VerificationToken row);

    List<VerificationToken> selectByExample(VerificationTokenExample example);

    long countByExample(VerificationTokenExample example);

    int deleteByExample(VerificationTokenExample example);

}
