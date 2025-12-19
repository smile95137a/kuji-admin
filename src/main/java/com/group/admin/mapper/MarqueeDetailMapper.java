package com.group.admin.mapper;

import com.group.admin.entity.MarqueeDetail;
import com.group.admin.example.MarqueeDetailExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MarqueeDetailMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(MarqueeDetail row);

    MarqueeDetail selectByPrimaryKey(@Param("id") Long id);

    List<MarqueeDetail> selectAll();

    int updateByPrimaryKey(MarqueeDetail row);

    List<MarqueeDetail> selectByExample(MarqueeDetailExample example);

    long countByExample(MarqueeDetailExample example);

    int deleteByExample(MarqueeDetailExample example);

}
