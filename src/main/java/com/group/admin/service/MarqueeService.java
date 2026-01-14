package com.group.admin.service;

import com.group.admin.entity.Marquee;

import java.util.List;

/**
 * 跑馬燈服務介面
 */
public interface MarqueeService {
    
    /**
     * 取得所有啟用中的跑馬燈（按優先級排序）
     */
    List<Marquee> getActiveMarquees();
    
    /**
     * 取得所有跑馬燈（管理用）
     */
    List<Marquee> getAllMarquees();
    
    /**
     * 根據 ID 取得跑馬燈
     */
    Marquee getMarqueeById(String id);
    
    /**
     * 新增跑馬燈
     */
    Marquee createMarquee(Marquee marquee);
    
    /**
     * 更新跑馬燈
     */
    Marquee updateMarquee(Marquee marquee);
    
    /**
     * 刪除跑馬燈
     */
    void deleteMarquee(String id);
    
    /**
     * 更新跑馬燈狀態（啟用/停用）
     */
    void updateStatus(String id, String status);
    
    /**
     * 廣播跑馬燈訊息（透過 WebSocket）
     */
    void broadcastMarquee(Marquee marquee);
    
    /**
     * 廣播所有啟用中的跑馬燈
     */
    void broadcastAllActiveMarquees();
}
