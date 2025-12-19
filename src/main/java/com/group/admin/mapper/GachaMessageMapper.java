package com.group.admin.mapper;

import com.group.admin.entity.GachaMessage;
import com.group.admin.example.GachaMessageExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GachaMessageMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(GachaMessage row);

    GachaMessage selectByPrimaryKey(@Param("id") Long id);

    List<GachaMessage> selectAll();

    int updateByPrimaryKey(GachaMessage row);

    List<GachaMessage> selectByExample(GachaMessageExample example);

    long countByExample(GachaMessageExample example);

    int deleteByExample(GachaMessageExample example);

}
