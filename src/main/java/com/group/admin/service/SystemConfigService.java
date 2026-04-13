package com.group.admin.service;

import com.group.admin.req.systemconfig.SystemConfigCreateReq;
import com.group.admin.req.systemconfig.SystemConfigUpdateReq;
import com.group.admin.res.systemconfig.SystemConfigRes;

import java.util.List;

public interface SystemConfigService {
    String KEY_PROTECTION_INITIAL_MINUTES = "protection_initial_minutes";
    String KEY_PROTECTION_EXTENSION_MINUTES = "protection_extension_minutes";
    String KEY_PROTECTION_MAX_MINUTES = "protection_max_minutes";
    String KEY_MAX_DRAWS_PER_REQUEST = "max_draws_per_request";

    SystemConfigRes create(SystemConfigCreateReq req);

    SystemConfigRes update(String id, SystemConfigUpdateReq req);

    void delete(String id);

    List<SystemConfigRes> listAll();

    List<SystemConfigRes> listByGroup(String group);

    int getInt(String key, int defaultValue);

    String getString(String key, String defaultValue);

    boolean getBoolean(String key, boolean defaultValue);
}
