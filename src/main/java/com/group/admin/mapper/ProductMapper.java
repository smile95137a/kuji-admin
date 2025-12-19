package com.group.admin.mapper;

import com.group.admin.entity.Product;
import com.group.admin.example.ProductExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(Product row);

    Product selectByPrimaryKey(@Param("id") String id);

    List<Product> selectAll();

    int updateByPrimaryKey(Product row);

    List<Product> selectByExample(ProductExample example);

    long countByExample(ProductExample example);

    int deleteByExample(ProductExample example);

}
