package com.group.admin.mapper;

import com.group.admin.entity.CustomerOrder;
import com.group.admin.example.CustomerOrderExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CustomerOrderMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(CustomerOrder row);

    CustomerOrder selectByPrimaryKey(@Param("id") String id);

    List<CustomerOrder> selectAll();

    int updateByPrimaryKey(CustomerOrder row);

    List<CustomerOrder> selectByExample(CustomerOrderExample example);

    long countByExample(CustomerOrderExample example);

    int deleteByExample(CustomerOrderExample example);

}
