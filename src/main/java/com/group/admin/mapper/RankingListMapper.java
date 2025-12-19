package com.group.admin.mapper;

import com.group.admin.entity.RankingList;
import com.group.admin.example.RankingListExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RankingListMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(RankingList row);

    RankingList selectByPrimaryKey(@Param("id") Long id);

    List<RankingList> selectAll();

    int updateByPrimaryKey(RankingList row);

    List<RankingList> selectByExample(RankingListExample example);

    long countByExample(RankingListExample example);

    int deleteByExample(RankingListExample example);

}
