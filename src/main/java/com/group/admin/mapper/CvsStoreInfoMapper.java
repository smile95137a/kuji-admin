package com.group.admin.mapper;

import com.group.admin.entity.CvsStoreInfo;
import com.group.admin.example.CvsStoreInfoExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CvsStoreInfoMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(CvsStoreInfo row);

    CvsStoreInfo selectByPrimaryKey(@Param("id") String id);

    List<CvsStoreInfo> selectAll();

    int updateByPrimaryKey(CvsStoreInfo row);

    List<CvsStoreInfo> selectByExample(CvsStoreInfoExample example);

    long countByExample(CvsStoreInfoExample example);

    int deleteByExample(CvsStoreInfoExample example);

}
