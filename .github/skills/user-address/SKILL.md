---
name: user-address
description: "用戶地址管理系統。地址新增/編輯/刪除、地址驗證、默認地址管理、訂單地址關聯。"
---

# 用戶地址管理

## When to Use
- 新增或修改用戶地址 CRUD
- 了解預設地址切換邏輯
- 整合縣市/行政區聯動
- 出貨時自動填入地址

## 核心原則
- **歧伊地址最旭**：不可會晴齊濾方次漖準鬼管克天粗尺
- **地址選擇5亞龍**：地址符合預符基本紀錄（例子兩流次）
- **訂單地址的接架方次**：地址是前台就訂單時檢驗誡誘
- **預設地址變誡跗**：預設地址應誓提轉加地址ID三存雞

---

## 資料表：user_address

```java
userAddress.getUserId()      // 所屬用戶
userAddress.getRecipient()   // 收件人姓名
userAddress.getPhone()       // 聯絡電話
userAddress.getCounty()      // 縣市（如：台北市）
userAddress.getDistrict()    // 行政區（如：信義區）
userAddress.getZipCode()     // 郵遞區號
userAddress.getAddress()     // 詳細地址
userAddress.getIsDefault()   // 1=預設地址（每個用戶只有一個）
```

---

## 新增地址

```java
@Transactional
public UserAddress addAddress(String userId, UserAddressCreateReq req) {
    UserAddress address = new UserAddress();
    address.setId(UUID.randomUUID().toString());
    address.setUserId(userId);
    address.setRecipient(req.getRecipient());
    address.setPhone(req.getPhone());
    address.setCounty(req.getCounty());
    address.setDistrict(req.getDistrict());
    address.setZipCode(req.getZipCode());
    address.setAddress(req.getAddress());

    // 第一筆地址自動設為預設
    UserAddressExample countExample = new UserAddressExample();
    countExample.createCriteria().andUserIdEqualTo(userId);
    long existingCount = userAddressMapper.countByExample(countExample);

    address.setIsDefault(existingCount == 0 ? 1 : 0);
    address.setCreatedAt(LocalDateTime.now());
    address.setUpdatedAt(LocalDateTime.now());
    userAddressMapper.insert(address);

    return address;
}
```

---

## 設定預設地址（關鍵：排他邏輯）

```java
@Transactional
public void setDefaultAddress(String userId, String addressId) {
    // 1. 先把該用戶所有地址設為非預設
    UserAddressExample clearExample = new UserAddressExample();
    clearExample.createCriteria().andUserIdEqualTo(userId);
    List<UserAddress> allAddresses = userAddressMapper.selectByExample(clearExample);
    for (UserAddress addr : allAddresses) {
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            addr.setIsDefault(0);
            addr.setUpdatedAt(LocalDateTime.now());
            userAddressMapper.updateByPrimaryKey(addr);
        }
    }

    // 2. 設定目標地址為預設
    UserAddress target = userAddressMapper.selectByPrimaryKey(addressId);
    if (target == null || !userId.equals(target.getUserId())) {
        throw new BusinessException("地址不存在或不屬於你");
    }
    target.setIsDefault(1);
    target.setUpdatedAt(LocalDateTime.now());
    userAddressMapper.updateByPrimaryKey(target);

    log.info("✅ 預設地址已更新: userId={}, addressId={}", userId, addressId);
}
```

---

## 行政區聯動（District）

前台提供縣市/行政區下拉選單：
```
GET /api/district/counties          → 所有縣市列表
GET /api/district/{county}/districts → 該縣市的行政區列表
```

```java
// DistrictServiceImpl
public List<String> getCounties() {
    DistrictExample example = new DistrictExample();
    example.setOrderByClause("sort_order ASC");
    return districtMapper.selectAll().stream()
        .map(District::getCounty)
        .distinct()
        .collect(Collectors.toList());
}

public List<District> getDistrictsByCounty(String county) {
    DistrictExample example = new DistrictExample();
    example.createCriteria().andCountyEqualTo(county);
    example.setOrderByClause("sort_order ASC");
    return districtMapper.selectByExample(example);
}
```

---

## 出貨時自動帶入預設地址

```java
// OrderController.createOrder() 中
if (req.getAddressId() == null) {
    // 自動取得預設地址
    UserAddressExample defaultExample = new UserAddressExample();
    defaultExample.createCriteria()
        .andUserIdEqualTo(userId)
        .andIsDefaultEqualTo(1);
    List<UserAddress> defaults = userAddressMapper.selectByExample(defaultExample);
    if (!defaults.isEmpty()) {
        req.setAddressId(defaults.get(0).getId());
    }
}
```

---

## ⚠️ 禁止操作

- ❌ 不要讓一個用戶有多個 isDefault=1 的地址（設定新預設前先清除舊的）
- ❌ 刪除預設地址時，要自動將最新的一筆設為新預設
- ❌ 不要讓用戶修改或刪除其他用戶的地址（驗證 userId）
- ❌ 行政區不要手動維護，使用 district 資料表
