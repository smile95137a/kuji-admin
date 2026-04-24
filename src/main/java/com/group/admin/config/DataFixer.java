package com.group.admin.config;

import com.group.admin.entity.User;
import com.group.admin.example.UserExample;
import com.group.admin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 一次性資料修復器（啟動時自動執行）
 *
 * <p>修復問題：舊版 loginWithGoogle() 會把 EMAIL 帳號的 provider 靜默改為 GOOGLE，
 * 導致用戶無法再用密碼登入。本類別在啟動時自動偵測並修復受影響的帳號。</p>
 *
 * <p>修復條件：provider = 'GOOGLE' 但 password != null（表示原本是 EMAIL 帳號被誤合併）</p>
 * <p>修復內容：provider → 'EMAIL', provider_id → NULL, email_verified → 1</p>
 *
 * @since 028
 */
@Slf4j
@Component
@Order(20)  // 在 DataInitializer(Order 10) 之後執行
@RequiredArgsConstructor
public class DataFixer implements CommandLineRunner {

    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        log.info("🔧 [DataFixer] 開始檢查 provider 損壞的帳號...");
        
        try {
            // 查詢 provider=GOOGLE 但有密碼的帳號（被舊版程式誤合併）
            UserExample example = new UserExample();
            example.createCriteria()
                    .andProviderEqualTo("GOOGLE")
                    .andPasswordIsNotNull();
            
            List<User> corruptedUsers = userMapper.selectByExample(example);
            
            if (corruptedUsers.isEmpty()) {
                log.info("✅ [DataFixer] 無需修復，未發現受影響帳號");
                return;
            }
            
            log.warn("⚠️ [DataFixer] 發現 {} 個 provider 損壞的帳號，開始修復...", corruptedUsers.size());
            
            int fixed = 0;
            for (User user : corruptedUsers) {
                try {
                    User update = new User();
                    update.setId(user.getId());
                    update.setProvider("EMAIL");
                    update.setProviderId(null);
                    update.setEmailVerified((byte) 1);  // 已存在的老帳號，直接標記為已驗證
                    update.setUpdatedAt(LocalDateTime.now());
                    userMapper.updateByPrimaryKeySelective(update);
                    log.info("✅ [DataFixer] 修復帳號: email={}, GOOGLE → EMAIL", user.getEmail());
                    fixed++;
                } catch (Exception e) {
                    log.error("❌ [DataFixer] 修復帳號失敗: email={}, error={}", user.getEmail(), e.getMessage());
                }
            }
            
            log.info("🎉 [DataFixer] 修復完成：成功 {}/{} 個帳號", fixed, corruptedUsers.size());
            
        } catch (Exception e) {
            // 修復失敗不影響應用程式啟動
            log.error("❌ [DataFixer] 修復過程發生錯誤: {}", e.getMessage(), e);
        }
    }
}
