package com.group.admin.service;

import com.group.admin.req.banner.BannerCondition;
import com.group.admin.req.banner.BannerCreateReq;
import com.group.admin.req.banner.BannerUpdateReq;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.banner.BannerRes;

import java.util.List;

public interface BannerService {

    List<BannerRes> queryBanners(QueryReq<BannerCondition> req);

    BannerRes getBannerById(String id);

    BannerRes createBanner(BannerCreateReq req);

    BannerRes updateBanner(String id, BannerUpdateReq req);

    void deleteBanner(String id);

    BannerRes publishBanner(String id);

    BannerRes unpublishBanner(String id);

    BannerRes updateBannerOrder(String id, Integer orderNum);

    void reorderBanners(List<String> ids);

    List<BannerRes> getCarouselBanners();

    void autoPublishBanners();

    void autoUnpublishBanners();

    void unpublishBannersByStoreId(String storeId);
}
