package com.group.admin.mapper;

import com.group.admin.entity.PrizeBox;
import com.group.admin.example.PrizeBoxExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PrizeBoxMapper {
    long countByExample(PrizeBoxExample example);

    int deleteByExample(PrizeBoxExample example);

    int deleteByPrimaryKey(String id);

    int insert(PrizeBox row);

    int insertSelective(PrizeBox row);

    List<PrizeBox> selectByExample(PrizeBoxExample example);

    PrizeBox selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") PrizeBox row, @Param("example") PrizeBoxExample example);

    int updateByExample(@Param("row") PrizeBox row, @Param("example") PrizeBoxExample example);

    int updateByPrimaryKeySelective(PrizeBox row);

    int updateByPrimaryKey(PrizeBox row);

    List<PrizeBox> selectByExampleWithPage(@Param("example") PrizeBoxExample example,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);
}