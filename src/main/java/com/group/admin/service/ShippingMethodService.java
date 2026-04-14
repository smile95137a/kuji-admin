package com.group.admin.service;

import com.group.admin.req.shippingmethod.ShippingMethodCreateReq;
import com.group.admin.req.shippingmethod.ShippingMethodUpdateReq;
import com.group.admin.res.shippingmethod.ShippingMethodRes;

import java.util.List;

public interface ShippingMethodService {

    List<ShippingMethodRes> listAll();

    List<ShippingMethodRes> listActive();

    ShippingMethodRes create(ShippingMethodCreateReq req);

    ShippingMethodRes update(String id, ShippingMethodUpdateReq req);

    void updateStatus(String id, String status);
}
