package com.group.admin.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 密碼工具類
 * 
 * <p>提供密碼生成、驗證等功能</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Component
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * 初始密碼最小長度
     */
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * 初始密碼最大長度
     */
    private static final int MAX_PASSWORD_LENGTH = 12;

    /**
     * 生成隨機初始密碼
     * 
     * <p>依據 store-account-management.prompt.md：
     * 建議 8-12 位，包含大小寫字母與數字</p>
     * 
     * @return 隨機密碼
     */
    public String generateRandomPassword() {
        int length = MIN_PASSWORD_LENGTH + RANDOM.nextInt(MAX_PASSWORD_LENGTH - MIN_PASSWORD_LENGTH + 1);
        
        // 確保包含至少一個大寫、一個小寫、一個數字
        StringBuilder password = new StringBuilder();
        password.append(RandomStringUtils.randomAlphabetic(1).toUpperCase());  // 1個大寫
        password.append(RandomStringUtils.randomAlphabetic(1).toLowerCase());  // 1個小寫
        password.append(RandomStringUtils.randomNumeric(1));                    // 1個數字
        password.append(RandomStringUtils.randomAlphanumeric(length - 3));     // 剩餘字元
        
        // 打亂順序
        return shuffleString(password.toString());
    }

    /**
     * 驗證密碼強度
     * 
     * <p>規則：至少 8 位，包含大小寫字母與數字</p>
     * 
     * @param password 待驗證的密碼
     * @return true 如果密碼符合規則
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasUpperCase && hasLowerCase && hasDigit;
    }

    /**
     * 取得密碼規則描述
     * 
     * @return 密碼規則說明
     */
    public String getPasswordRules() {
        return "密碼需至少 8 位，並包含大寫字母、小寫字母及數字";
    }

    /**
     * 打亂字串順序
     */
    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
