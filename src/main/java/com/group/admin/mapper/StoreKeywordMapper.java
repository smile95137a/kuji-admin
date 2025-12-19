package com.group.admin.mapper;

import com.group.admin.entity.StoreKeyword;
import com.group.admin.example.StoreKeywordExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreKeywordMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(StoreKeyword row);

    StoreKeyword selectByPrimaryKey(@Param("id") Long id);

    List<StoreKeyword> selectAll();

    int updateByPrimaryKey(StoreKeyword row);

    List<StoreKeyword> selectByExample(StoreKeywordExample example);

    long countByExample(StoreKeywordExample example);

    int deleteByExample(StoreKeywordExample example);

}
