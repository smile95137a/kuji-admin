package com.group.admin.mapper;

import com.group.admin.entity.UserFavoriteStoreProducts;
import com.group.admin.example.UserFavoriteStoreProductsExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserFavoriteStoreProductsMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(UserFavoriteStoreProducts row);

    UserFavoriteStoreProducts selectByPrimaryKey(@Param("id") Long id);

    List<UserFavoriteStoreProducts> selectAll();

    int updateByPrimaryKey(UserFavoriteStoreProducts row);

    List<UserFavoriteStoreProducts> selectByExample(UserFavoriteStoreProductsExample example);

    long countByExample(UserFavoriteStoreProductsExample example);

    int deleteByExample(UserFavoriteStoreProductsExample example);

}
