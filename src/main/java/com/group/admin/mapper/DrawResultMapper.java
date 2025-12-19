package com.group.admin.mapper;

import com.group.admin.entity.DrawResult;
import com.group.admin.example.DrawResultExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DrawResultMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(DrawResult row);

    DrawResult selectByPrimaryKey(@Param("id") String id);

    List<DrawResult> selectAll();

    int updateByPrimaryKey(DrawResult row);

    List<DrawResult> selectByExample(DrawResultExample example);

    long countByExample(DrawResultExample example);

    int deleteByExample(DrawResultExample example);

}
