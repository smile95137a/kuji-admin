package com.group.admin.mapper;

import com.group.admin.entity.Marquee;
import com.group.admin.example.MarqueeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarqueeMapper {
    long countByExample(MarqueeExample example);

    int deleteByExample(MarqueeExample example);

    int deleteByPrimaryKey(String id);

    int insert(Marquee row);

    int insertSelective(Marquee row);

    List<Marquee> selectByExample(MarqueeExample example);

    Marquee selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Marquee row, @Param("example") MarqueeExample example);

    int updateByExample(@Param("row") Marquee row, @Param("example") MarqueeExample example);

    int updateByPrimaryKeySelective(Marquee row);

    int updateByPrimaryKey(Marquee row);
}