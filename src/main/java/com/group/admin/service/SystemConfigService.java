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
    String KEY_DRAW_PROTECTION_BASE_SECONDS = "draw_protection_base_seconds";
    String KEY_DRAW_PROTECTION_EXTRA_SECONDS_PER_DRAW = "draw_protection_extra_seconds_per_draw";
    String KEY_DRAW_PROTECTION_MAX_SECONDS = "draw_protection_max_seconds";
    String KEY_DRAW_BONUS_TITLE = "draw_bonus_title";
    String KEY_DRAW_BONUS_DESCRIPTION = "draw_bonus_description";
    String KEY_DRAW_PROTECTION_TITLE = "draw_protection_title";
    String KEY_DRAW_PROTECTION_DESCRIPTION = "draw_protection_description";
    String KEY_DRAW_BONUS_TIERS_JSON = "draw_bonus_tiers_json";

    SystemConfigRes create(SystemConfigCreateReq req);

    SystemConfigRes update(String id, SystemConfigUpdateReq req);

    void delete(String id);

    List<SystemConfigRes> listAll();

    List<SystemConfigRes> listByGroup(String group);

    int getInt(String key, int defaultValue);

    String getString(String key, String defaultValue);

    boolean getBoolean(String key, boolean defaultValue);
}
