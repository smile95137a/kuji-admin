package com.group.admin.mapper;

import com.group.admin.entity.Marquee;
import com.group.admin.example.MarqueeExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MarqueeMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(Marquee row);

    Marquee selectByPrimaryKey(@Param("id") Long id);

    List<Marquee> selectAll();

    int updateByPrimaryKey(Marquee row);

    List<Marquee> selectByExample(MarqueeExample example);

    long countByExample(MarqueeExample example);

    int deleteByExample(MarqueeExample example);

}
