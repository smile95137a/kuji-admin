# ⚠️ 編譯錯誤總結與修復方案

## 📌 問題分析

編譯失敗的主要原因：

1. **Entity 類別缺少 Getter/Setter**
   - Lombok 的 `@Data` 註解沒有生效
   - 需要在所有 Entity 加上 `@Data` 並確保 Lombok 正常運作

2. **部分類別缺少 `@Builder` 註解**
   - `ApiResponse`、`PageResponse`、`ErrorInfo`、`MetaInfo` 等類別使用了 `.builder()`，但沒有 `@Builder` 註解

3. **AdminJwtAuthenticationFilter 檔案損壞**
   - 編譯器無法找到這個類別
   - 需要重新建立

4. **部分類別缺少 `@Slf4j` 註解**
   - `GlobalResponseAspect`、`GlobalExceptionHandler` 使用了 `log`，但沒有 `@Slf4j`

---

## ✅ 修復方案（請依照順序執行）

### 步驟 1：檢查 pom.xml 中的 Lombok 配置

確認 `pom.xml` 中有以下配置：

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${lombok.version}</version>
    <scope>provided</scope>
</dependency>

<!-- 在 maven-compiler-plugin 中配置 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### 步驟 2：為所有 Entity 類別加上完整的 Lombok 註解

以下是需要修改的 Entity 列表：

#### User.java
```java
package com.group.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String email;
    private String nickname;
    private String password;
    private String avatar;
    private Long goldCoins;
    private Long bonusCoins;
    private Integer status;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
```

#### LotteryPrize.java
```java
package com.group.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryPrize {
    private String id;
    private String lotteryId;
    private String prizeName;
    private String prizeImageUrl;
    private Integer remaining;
    private Integer weight;
    private Integer prizeRank;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
```

#### Lottery.java
```java
package com.group.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lottery {
    private String id;
    private String storeId;
    private String lotteryName;
    private String description;
    private String imageUrl;
    private Long pricePerDraw;
    private Integer totalPrizes;
    private Integer status;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
```

#### PointLog.java、LotteryDrawRecord.java 等其他 Entity
同樣加上 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`

---

### 步驟 3：為 Result 類別加上 @Builder

#### ApiResponse.java（已修改，需要加 @Builder）
確認有這些註解：
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    // ...
}
```

#### ErrorInfo.java、MetaInfo.java、PageResponse.java
同樣確認有 `@Builder` 註解

---

### 步驟 4：為 AOP 和 Handler 加上 @Slf4j

#### GlobalResponseAspect.java
```java
package com.group.admin.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j  // ← 加上這個
@Aspect
@Component
public class GlobalResponseAspect {
    // ...
}
```

#### GlobalExceptionHandler.java
```java
package com.group.admin.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j  // ← 加上這個
@RestControllerAdvice
public class GlobalExceptionHandler {
    // ...
}
```

---

### 步驟 5：修復 AdminJwtAuthenticationFilter

刪除並重新建立這個檔案（我已經提供過正確的版本）

---

### 步驟 6：清理並重新編譯

```bash
cd "c:\Users\user\OneDrive\Desktop\創業\KUJI-Server\admin"
mvn clean
mvn compile -DskipTests
```

---

## 🔧 快速修復腳本

由於錯誤太多，我建議你：

### 選項 A：手動修改
1. 打開每個 Entity 類別
2. 加上 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
3. 確保所有欄位都有定義

### 選項 B：使用 MyBatis Generator 重新產生 Entity
1. 執行 MyBatis Generator
2. 自動產生所有 Entity 類別（會包含完整的 Getter/Setter）

---

## 📝 建議

由於你的專案目前有很多編譯錯誤，我建議：

1. **先不要執行新增的 Security 相關程式碼**
   - 先把原有的程式碼修正到可以編譯
   - 再逐步整合新的架構

2. **使用 MyBatis Generator 重新產生 Entity**
   - 這樣可以確保所有 Entity 都有完整的 Getter/Setter
   - 配置檔在 `src/main/resources/mapper/generatorConfig.xml`

3. **分階段進行**
   - 階段 1：修復 Entity 類別（讓專案可以編譯）
   - 階段 2：整合新的 Security 架構
   - 階段 3：實作 CRUD Controller
   - 階段 4：撰寫測試

---

## ❓ 下一步

請告訴我你想要：

**選項 A：我幫你修復所有 Entity 類別**
- 我會逐一修改每個 Entity
- 加上完整的 Lombok 註解
- 確保可以編譯

**選項 B：你自己使用 MyBatis Generator 重新產生**
- 我提供 generatorConfig.xml 配置
- 你執行 `mvn mybatis-generator:generate`
- 自動產生所有 Entity

**選項 C：先擱置 Security 改造，專注在 CRUD 實作**
- 恢復原本的 SecurityConfig
- 先把後台管理功能做出來
- 之後再整合新的 Security 架構

請告訴我你想要哪個選項！
