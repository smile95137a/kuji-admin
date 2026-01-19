package com.group.admin.mapper;

import com.group.admin.entity.EmailLog;
import com.group.admin.example.EmailLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EmailLogMapper {
    long countByExample(EmailLogExample example);

    int deleteByExample(EmailLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(EmailLog row);

    int insertSelective(EmailLog row);

    List<EmailLog> selectByExampleWithBLOBs(EmailLogExample example);

    List<EmailLog> selectByExample(EmailLogExample example);

    EmailLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") EmailLog row, @Param("example") EmailLogExample example);

    int updateByExampleWithBLOBs(@Param("row") EmailLog row, @Param("example") EmailLogExample example);

    int updateByExample(@Param("row") EmailLog row, @Param("example") EmailLogExample example);

    int updateByPrimaryKeySelective(EmailLog row);

    int updateByPrimaryKeyWithBLOBs(EmailLog row);

    int updateByPrimaryKey(EmailLog row);
}