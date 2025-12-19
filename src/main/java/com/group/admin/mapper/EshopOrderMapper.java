package com.group.admin.mapper;

import com.group.admin.entity.EshopOrder;
import com.group.admin.example.EshopOrderExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EshopOrderMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(EshopOrder row);

    EshopOrder selectByPrimaryKey(@Param("id") String id);

    List<EshopOrder> selectAll();

    int updateByPrimaryKey(EshopOrder row);

    List<EshopOrder> selectByExample(EshopOrderExample example);

    long countByExample(EshopOrderExample example);

    int deleteByExample(EshopOrderExample example);

}
