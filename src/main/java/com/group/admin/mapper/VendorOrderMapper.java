package com.group.admin.mapper;

import com.group.admin.entity.VendorOrder;
import com.group.admin.example.VendorOrderExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VendorOrderMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(VendorOrder row);

    VendorOrder selectByPrimaryKey(@Param("id") String id);

    List<VendorOrder> selectAll();

    int updateByPrimaryKey(VendorOrder row);

    List<VendorOrder> selectByExample(VendorOrderExample example);

    long countByExample(VendorOrderExample example);

    int deleteByExample(VendorOrderExample example);

}
