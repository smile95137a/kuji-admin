-- 檢查 District 表結構與資料

-- 1. 檢查表結構
DESCRIBE district;

-- 2. 檢查資料筆數
SELECT COUNT(*) AS total_count FROM district;

-- 3. 檢查有哪些縣市
SELECT DISTINCT city FROM district ORDER BY city;

-- 4. 檢查台北市的行政區
SELECT * FROM district WHERE city = '台北市' ORDER BY district;

-- 5. 如果 district 表為空，則初始化資料
-- 執行以下 INSERT 語句（台北市示例）

INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '台北市', '中正區', '100', NOW(), NOW()),
(UUID(), '台北市', '大同區', '103', NOW(), NOW()),
(UUID(), '台北市', '中山區', '104', NOW(), NOW()),
(UUID(), '台北市', '松山區', '105', NOW(), NOW()),
(UUID(), '台北市', '大安區', '106', NOW(), NOW()),
(UUID(), '台北市', '萬華區', '108', NOW(), NOW()),
(UUID(), '台北市', '信義區', '110', NOW(), NOW()),
(UUID(), '台北市', '士林區', '111', NOW(), NOW()),
(UUID(), '台北市', '北投區', '112', NOW(), NOW()),
(UUID(), '台北市', '內湖區', '114', NOW(), NOW()),
(UUID(), '台北市', '南港區', '115', NOW(), NOW()),
(UUID(), '台北市', '文山區', '116', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 新北市
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '新北市', '板橋區', '220', NOW(), NOW()),
(UUID(), '新北市', '三重區', '241', NOW(), NOW()),
(UUID(), '新北市', '中和區', '235', NOW(), NOW()),
(UUID(), '新北市', '永和區', '234', NOW(), NOW()),
(UUID(), '新北市', '新莊區', '242', NOW(), NOW()),
(UUID(), '新北市', '新店區', '231', NOW(), NOW()),
(UUID(), '新北市', '樹林區', '238', NOW(), NOW()),
(UUID(), '新北市', '鶯歌區', '239', NOW(), NOW()),
(UUID(), '新北市', '三峽區', '237', NOW(), NOW()),
(UUID(), '新北市', '淡水區', '251', NOW(), NOW()),
(UUID(), '新北市', '汐止區', '221', NOW(), NOW()),
(UUID(), '新北市', '瑞芳區', '224', NOW(), NOW()),
(UUID(), '新北市', '土城區', '236', NOW(), NOW()),
(UUID(), '新北市', '蘆洲區', '247', NOW(), NOW()),
(UUID(), '新北市', '五股區', '248', NOW(), NOW()),
(UUID(), '新北市', '泰山區', '243', NOW(), NOW()),
(UUID(), '新北市', '林口區', '244', NOW(), NOW()),
(UUID(), '新北市', '深坑區', '222', NOW(), NOW()),
(UUID(), '新北市', '石碇區', '223', NOW(), NOW()),
(UUID(), '新北市', '坪林區', '232', NOW(), NOW()),
(UUID(), '新北市', '三芝區', '252', NOW(), NOW()),
(UUID(), '新北市', '石門區', '253', NOW(), NOW()),
(UUID(), '新北市', '八里區', '249', NOW(), NOW()),
(UUID(), '新北市', '平溪區', '226', NOW(), NOW()),
(UUID(), '新北市', '雙溪區', '227', NOW(), NOW()),
(UUID(), '新北市', '貢寮區', '228', NOW(), NOW()),
(UUID(), '新北市', '金山區', '208', NOW(), NOW()),
(UUID(), '新北市', '萬里區', '207', NOW(), NOW()),
(UUID(), '新北市', '烏來區', '233', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 桃園市
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '桃園市', '桃園區', '330', NOW(), NOW()),
(UUID(), '桃園市', '中壢區', '320', NOW(), NOW()),
(UUID(), '桃園市', '平鎮區', '324', NOW(), NOW()),
(UUID(), '桃園市', '八德區', '334', NOW(), NOW()),
(UUID(), '桃園市', '楊梅區', '326', NOW(), NOW()),
(UUID(), '桃園市', '蘆竹區', '338', NOW(), NOW()),
(UUID(), '桃園市', '大溪區', '335', NOW(), NOW()),
(UUID(), '桃園市', '龍潭區', '325', NOW(), NOW()),
(UUID(), '桃園市', '龜山區', '333', NOW(), NOW()),
(UUID(), '桃園市', '大園區', '337', NOW(), NOW()),
(UUID(), '桃園市', '觀音區', '328', NOW(), NOW()),
(UUID(), '桃園市', '新屋區', '327', NOW(), NOW()),
(UUID(), '桃園市', '復興區', '336', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 台中市
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '台中市', '中區', '400', NOW(), NOW()),
(UUID(), '台中市', '東區', '401', NOW(), NOW()),
(UUID(), '台中市', '南區', '402', NOW(), NOW()),
(UUID(), '台中市', '西區', '403', NOW(), NOW()),
(UUID(), '台中市', '北區', '404', NOW(), NOW()),
(UUID(), '台中市', '北屯區', '406', NOW(), NOW()),
(UUID(), '台中市', '西屯區', '407', NOW(), NOW()),
(UUID(), '台中市', '南屯區', '408', NOW(), NOW()),
(UUID(), '台中市', '太平區', '411', NOW(), NOW()),
(UUID(), '台中市', '大里區', '412', NOW(), NOW()),
(UUID(), '台中市', '霧峰區', '413', NOW(), NOW()),
(UUID(), '台中市', '烏日區', '414', NOW(), NOW()),
(UUID(), '台中市', '豐原區', '420', NOW(), NOW()),
(UUID(), '台中市', '后里區', '421', NOW(), NOW()),
(UUID(), '台中市', '石岡區', '422', NOW(), NOW()),
(UUID(), '台中市', '東勢區', '423', NOW(), NOW()),
(UUID(), '台中市', '和平區', '424', NOW(), NOW()),
(UUID(), '台中市', '新社區', '426', NOW(), NOW()),
(UUID(), '台中市', '潭子區', '427', NOW(), NOW()),
(UUID(), '台中市', '大雅區', '428', NOW(), NOW()),
(UUID(), '台中市', '神岡區', '429', NOW(), NOW()),
(UUID(), '台中市', '大肚區', '432', NOW(), NOW()),
(UUID(), '台中市', '沙鹿區', '433', NOW(), NOW()),
(UUID(), '台中市', '龍井區', '434', NOW(), NOW()),
(UUID(), '台中市', '梧棲區', '435', NOW(), NOW()),
(UUID(), '台中市', '清水區', '436', NOW(), NOW()),
(UUID(), '台中市', '大甲區', '437', NOW(), NOW()),
(UUID(), '台中市', '外埔區', '438', NOW(), NOW()),
(UUID(), '台中市', '大安區', '439', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 台南市
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '台南市', '中西區', '700', NOW(), NOW()),
(UUID(), '台南市', '東區', '701', NOW(), NOW()),
(UUID(), '台南市', '南區', '702', NOW(), NOW()),
(UUID(), '台南市', '北區', '704', NOW(), NOW()),
(UUID(), '台南市', '安平區', '708', NOW(), NOW()),
(UUID(), '台南市', '安南區', '709', NOW(), NOW()),
(UUID(), '台南市', '永康區', '710', NOW(), NOW()),
(UUID(), '台南市', '歸仁區', '711', NOW(), NOW()),
(UUID(), '台南市', '新化區', '712', NOW(), NOW()),
(UUID(), '台南市', '左鎮區', '713', NOW(), NOW()),
(UUID(), '台南市', '玉井區', '714', NOW(), NOW()),
(UUID(), '台南市', '楠西區', '715', NOW(), NOW()),
(UUID(), '台南市', '南化區', '716', NOW(), NOW()),
(UUID(), '台南市', '仁德區', '717', NOW(), NOW()),
(UUID(), '台南市', '關廟區', '718', NOW(), NOW()),
(UUID(), '台南市', '龍崎區', '719', NOW(), NOW()),
(UUID(), '台南市', '官田區', '720', NOW(), NOW()),
(UUID(), '台南市', '麻豆區', '721', NOW(), NOW()),
(UUID(), '台南市', '佳里區', '722', NOW(), NOW()),
(UUID(), '台南市', '西港區', '723', NOW(), NOW()),
(UUID(), '台南市', '七股區', '724', NOW(), NOW()),
(UUID(), '台南市', '將軍區', '725', NOW(), NOW()),
(UUID(), '台南市', '學甲區', '726', NOW(), NOW()),
(UUID(), '台南市', '北門區', '727', NOW(), NOW()),
(UUID(), '台南市', '新營區', '730', NOW(), NOW()),
(UUID(), '台南市', '後壁區', '731', NOW(), NOW()),
(UUID(), '台南市', '白河區', '732', NOW(), NOW()),
(UUID(), '台南市', '東山區', '733', NOW(), NOW()),
(UUID(), '台南市', '六甲區', '734', NOW(), NOW()),
(UUID(), '台南市', '下營區', '735', NOW(), NOW()),
(UUID(), '台南市', '柳營區', '736', NOW(), NOW()),
(UUID(), '台南市', '鹽水區', '737', NOW(), NOW()),
(UUID(), '台南市', '善化區', '741', NOW(), NOW()),
(UUID(), '台南市', '大內區', '742', NOW(), NOW()),
(UUID(), '台南市', '山上區', '743', NOW(), NOW()),
(UUID(), '台南市', '新市區', '744', NOW(), NOW()),
(UUID(), '台南市', '安定區', '745', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 高雄市
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '高雄市', '新興區', '800', NOW(), NOW()),
(UUID(), '高雄市', '前金區', '801', NOW(), NOW()),
(UUID(), '高雄市', '苓雅區', '802', NOW(), NOW()),
(UUID(), '高雄市', '鹽埕區', '803', NOW(), NOW()),
(UUID(), '高雄市', '鼓山區', '804', NOW(), NOW()),
(UUID(), '高雄市', '旗津區', '805', NOW(), NOW()),
(UUID(), '高雄市', '前鎮區', '806', NOW(), NOW()),
(UUID(), '高雄市', '三民區', '807', NOW(), NOW()),
(UUID(), '高雄市', '楠梓區', '811', NOW(), NOW()),
(UUID(), '高雄市', '小港區', '812', NOW(), NOW()),
(UUID(), '高雄市', '左營區', '813', NOW(), NOW()),
(UUID(), '高雄市', '仁武區', '814', NOW(), NOW()),
(UUID(), '高雄市', '大社區', '815', NOW(), NOW()),
(UUID(), '高雄市', '東沙群島', '817', NOW(), NOW()),
(UUID(), '高雄市', '南沙群島', '819', NOW(), NOW()),
(UUID(), '高雄市', '岡山區', '820', NOW(), NOW()),
(UUID(), '高雄市', '路竹區', '821', NOW(), NOW()),
(UUID(), '高雄市', '阿蓮區', '822', NOW(), NOW()),
(UUID(), '高雄市', '田寮區', '823', NOW(), NOW()),
(UUID(), '高雄市', '燕巢區', '824', NOW(), NOW()),
(UUID(), '高雄市', '橋頭區', '825', NOW(), NOW()),
(UUID(), '高雄市', '梓官區', '826', NOW(), NOW()),
(UUID(), '高雄市', '彌陀區', '827', NOW(), NOW()),
(UUID(), '高雄市', '永安區', '828', NOW(), NOW()),
(UUID(), '高雄市', '湖內區', '829', NOW(), NOW()),
(UUID(), '高雄市', '鳳山區', '830', NOW(), NOW()),
(UUID(), '高雄市', '大寮區', '831', NOW(), NOW()),
(UUID(), '高雄市', '林園區', '832', NOW(), NOW()),
(UUID(), '高雄市', '鳥松區', '833', NOW(), NOW()),
(UUID(), '高雄市', '大樹區', '840', NOW(), NOW()),
(UUID(), '高雄市', '旗山區', '842', NOW(), NOW()),
(UUID(), '高雄市', '美濃區', '843', NOW(), NOW()),
(UUID(), '高雄市', '六龜區', '844', NOW(), NOW()),
(UUID(), '高雄市', '內門區', '845', NOW(), NOW()),
(UUID(), '高雄市', '杉林區', '846', NOW(), NOW()),
(UUID(), '高雄市', '甲仙區', '847', NOW(), NOW()),
(UUID(), '高雄市', '桃源區', '848', NOW(), NOW()),
(UUID(), '高雄市', '那瑪夏區', '849', NOW(), NOW()),
(UUID(), '高雄市', '茂林區', '851', NOW(), NOW()),
(UUID(), '高雄市', '茄萣區', '852', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 6. 檢查初始化結果
SELECT city, COUNT(*) AS district_count 
FROM district 
GROUP BY city 
ORDER BY city;
