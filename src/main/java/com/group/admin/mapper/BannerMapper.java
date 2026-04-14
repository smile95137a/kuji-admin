package com.group.admin.mapper;

import com.group.admin.entity.Banner;
import com.group.admin.example.BannerExample;
import com.group.admin.req.banner.BannerCondition;
import com.group.admin.res.banner.BannerRes;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BannerMapper {
    long countByExample(BannerExample example);

    int deleteByExample(BannerExample example);

    int deleteByPrimaryKey(String id);

    int insert(Banner row);

    int insertSelective(Banner row);

    List<Banner> selectByExample(BannerExample example);

    Banner selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Banner row, @Param("example") BannerExample example);

    int updateByExample(@Param("row") Banner row, @Param("example") BannerExample example);

    int updateByPrimaryKeySelective(Banner row);

    int updateByPrimaryKey(Banner row);

    /** Admin list query: LEFT JOIN store, supports dynamic conditions */
    List<BannerRes> selectList(BannerCondition condition);

    /** Public carousel query: INNER JOIN store WHERE store.status=ACTIVE and banner in time range */
    List<BannerRes> selectActiveBanners();

    /** Scheduled publish: set status=PUBLISHED where start_time reached */
    int autoPublishBanners();

    /** Scheduled unpublish: set status=UNPUBLISHED where end_time passed */
    int autoUnpublishBanners();

    /** Unpublish all banners for a store (when store is deactivated) */
    int unpublishBannersByStoreId(@Param("storeId") String storeId);
}