package com.group.admin.service.impl;

import com.group.admin.entity.Marquee;
import com.group.admin.mapper.MarqueeMapper;
import com.group.admin.repository.MarqueeRepository;
import com.group.admin.service.MarqueeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 跑馬燈服務實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarqueeServiceImpl implements MarqueeService {
    
    private final MarqueeMapper marqueeMapper;
    private final MarqueeRepository marqueeRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public List<Marquee> getActiveMarquees() {
        return marqueeRepository.selectActiveMarquees(LocalDateTime.now());
    }
    
    @Override
    public List<Marquee> getAllMarquees() {
        return marqueeRepository.selectAll();
    }
    
    @Override
    public Marquee getMarqueeById(String id) {
        return marqueeRepository.selectById(id);
    }
    
    @Override
    public Marquee createMarquee(Marquee marquee) {
        marquee.setId(UUID.randomUUID().toString());
        marquee.setCreatedAt(LocalDateTime.now());
        marquee.setUpdatedAt(LocalDateTime.now());
        
        // 設定預設值
        if (marquee.getIsActive() == null) {
            marquee.setIsActive(true);  // 預設啟用
        }
        if (marquee.getPriority() == null) {
            marquee.setPriority(0);
        }
        
        marqueeMapper.insert(marquee);
        log.info("✅ 跑馬燈已建立: id={}, content={}", marquee.getId(), marquee.getContent());
        
        // 如果狀態為啟用，立即廣播
        if (Boolean.TRUE.equals(marquee.getIsActive())) {
            broadcastMarquee(marquee);
        }
        
        return marquee;
    }
    
    @Override
    public Marquee updateMarquee(Marquee marquee) {
        marquee.setUpdatedAt(LocalDateTime.now());
        marqueeRepository.update(marquee);
        log.info("✅ 跑馬燈已更新: id={}", marquee.getId());
        
        // 廣播更新後的跑馬燈列表
        broadcastAllActiveMarquees();
        
        return marquee;
    }
    
    @Override
    public void deleteMarquee(String id) {
        marqueeRepository.deleteById(id);
        log.info("🗑️ 跑馬燈已刪除: id={}", id);
        
        // 廣播更新後的跑馬燈列表
        broadcastAllActiveMarquees();
    }
    
    @Override
    public void updateStatus(String id, String status) {
        Byte isActive = "ACTIVE".equals(status) ? (byte) 1 : (byte) 0;
        marqueeRepository.updateStatus(id, isActive, LocalDateTime.now());
        log.info("✅ 跑馬燈狀態已更新: id={}, status={}", id, status);
        
        // 廣播更新後的跑馬燈列表
        broadcastAllActiveMarquees();
    }
    
    @Override
    public void broadcastMarquee(Marquee marquee) {
        try {
            if (marquee != null) {
                // 廣播單條跑馬燈到 /topic/marquee
                messagingTemplate.convertAndSend("/topic/marquee", marquee);
                log.debug("📢 跑馬燈已廣播: content={}", marquee.getContent());
            }
        } catch (Exception e) {
            log.error("❌ 跑馬燈廣播失敗: {}", e.getMessage());
        }
    }
    
    @Override
    public void broadcastAllActiveMarquees() {
        try {
            List<Marquee> activeMarquees = getActiveMarquees();
            if (activeMarquees != null && !activeMarquees.isEmpty()) {
                // 廣播所有啟用中的跑馬燈到 /topic/marquees
                messagingTemplate.convertAndSend("/topic/marquees", activeMarquees);
                log.debug("📢 跑馬燈列表已廣播: count={}", activeMarquees.size());
            }
        } catch (Exception e) {
            log.error("❌ 跑馬燈列表廣播失敗: {}", e.getMessage());
        }
    }
}
