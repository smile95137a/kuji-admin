package com.group.admin.mapper;

import com.group.admin.entity.Draw;
import com.group.admin.example.DrawExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DrawMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(Draw row);

    Draw selectByPrimaryKey(@Param("id") Long id);

    List<Draw> selectAll();

    int updateByPrimaryKey(Draw row);

    List<Draw> selectByExample(DrawExample example);

    long countByExample(DrawExample example);

    int deleteByExample(DrawExample example);

}
