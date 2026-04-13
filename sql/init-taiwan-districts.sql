-- ========================================
-- 台灣完整行政區資料初始化
-- 資料來源：前端 TypeScript 介面
-- 總計：22 個縣市 + 368 個鄉鎮市區
-- ========================================

USE kuji_db;

-- 清空舊資料（如果需要）
-- TRUNCATE TABLE district;

-- 1. 臺北市（12 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '臺北市', '中正區', '100', NOW(), NOW()),
(UUID(), '臺北市', '大同區', '103', NOW(), NOW()),
(UUID(), '臺北市', '中山區', '104', NOW(), NOW()),
(UUID(), '臺北市', '松山區', '105', NOW(), NOW()),
(UUID(), '臺北市', '大安區', '106', NOW(), NOW()),
(UUID(), '臺北市', '萬華區', '108', NOW(), NOW()),
(UUID(), '臺北市', '信義區', '110', NOW(), NOW()),
(UUID(), '臺北市', '士林區', '111', NOW(), NOW()),
(UUID(), '臺北市', '北投區', '112', NOW(), NOW()),
(UUID(), '臺北市', '內湖區', '114', NOW(), NOW()),
(UUID(), '臺北市', '南港區', '115', NOW(), NOW()),
(UUID(), '臺北市', '文山區', '116', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 2. 基隆市（7 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '基隆市', '仁愛區', '200', NOW(), NOW()),
(UUID(), '基隆市', '信義區', '201', NOW(), NOW()),
(UUID(), '基隆市', '中正區', '202', NOW(), NOW()),
(UUID(), '基隆市', '中山區', '203', NOW(), NOW()),
(UUID(), '基隆市', '安樂區', '204', NOW(), NOW()),
(UUID(), '基隆市', '暖暖區', '205', NOW(), NOW()),
(UUID(), '基隆市', '七堵區', '206', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 3. 新北市（29 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '新北市', '萬里區', '207', NOW(), NOW()),
(UUID(), '新北市', '金山區', '208', NOW(), NOW()),
(UUID(), '新北市', '板橋區', '220', NOW(), NOW()),
(UUID(), '新北市', '汐止區', '221', NOW(), NOW()),
(UUID(), '新北市', '深坑區', '222', NOW(), NOW()),
(UUID(), '新北市', '石碇區', '223', NOW(), NOW()),
(UUID(), '新北市', '瑞芳區', '224', NOW(), NOW()),
(UUID(), '新北市', '平溪區', '226', NOW(), NOW()),
(UUID(), '新北市', '雙溪區', '227', NOW(), NOW()),
(UUID(), '新北市', '貢寮區', '228', NOW(), NOW()),
(UUID(), '新北市', '新店區', '231', NOW(), NOW()),
(UUID(), '新北市', '坪林區', '232', NOW(), NOW()),
(UUID(), '新北市', '烏來區', '233', NOW(), NOW()),
(UUID(), '新北市', '永和區', '234', NOW(), NOW()),
(UUID(), '新北市', '中和區', '235', NOW(), NOW()),
(UUID(), '新北市', '土城區', '236', NOW(), NOW()),
(UUID(), '新北市', '三峽區', '237', NOW(), NOW()),
(UUID(), '新北市', '樹林區', '238', NOW(), NOW()),
(UUID(), '新北市', '鶯歌區', '239', NOW(), NOW()),
(UUID(), '新北市', '三重區', '241', NOW(), NOW()),
(UUID(), '新北市', '新莊區', '242', NOW(), NOW()),
(UUID(), '新北市', '泰山區', '243', NOW(), NOW()),
(UUID(), '新北市', '林口區', '244', NOW(), NOW()),
(UUID(), '新北市', '蘆洲區', '247', NOW(), NOW()),
(UUID(), '新北市', '五股區', '248', NOW(), NOW()),
(UUID(), '新北市', '八里區', '249', NOW(), NOW()),
(UUID(), '新北市', '淡水區', '251', NOW(), NOW()),
(UUID(), '新北市', '三芝區', '252', NOW(), NOW()),
(UUID(), '新北市', '石門區', '253', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 4. 連江縣（4 個鄉）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '連江縣', '南竿鄉', '209', NOW(), NOW()),
(UUID(), '連江縣', '北竿鄉', '210', NOW(), NOW()),
(UUID(), '連江縣', '莒光鄉', '211', NOW(), NOW()),
(UUID(), '連江縣', '東引鄉', '212', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 5. 宜蘭縣（13 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '宜蘭縣', '宜蘭市', '260', NOW(), NOW()),
(UUID(), '宜蘭縣', '壯圍鄉', '263', NOW(), NOW()),
(UUID(), '宜蘭縣', '頭城鎮', '261', NOW(), NOW()),
(UUID(), '宜蘭縣', '礁溪鄉', '262', NOW(), NOW()),
(UUID(), '宜蘭縣', '員山鄉', '264', NOW(), NOW()),
(UUID(), '宜蘭縣', '羅東鎮', '265', NOW(), NOW()),
(UUID(), '宜蘭縣', '三星鄉', '266', NOW(), NOW()),
(UUID(), '宜蘭縣', '大同鄉', '267', NOW(), NOW()),
(UUID(), '宜蘭縣', '五結鄉', '268', NOW(), NOW()),
(UUID(), '宜蘭縣', '冬山鄉', '269', NOW(), NOW()),
(UUID(), '宜蘭縣', '蘇澳鎮', '270', NOW(), NOW()),
(UUID(), '宜蘭縣', '南澳鄉', '272', NOW(), NOW()),
(UUID(), '宜蘭縣', '釣魚臺', '290', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 6. 新竹市（3 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '新竹市', '東區', '300', NOW(), NOW()),
(UUID(), '新竹市', '北區', '300', NOW(), NOW()),
(UUID(), '新竹市', '香山區', '300', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 7. 新竹縣（13 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '新竹縣', '寶山鄉', '308', NOW(), NOW()),
(UUID(), '新竹縣', '竹北市', '302', NOW(), NOW()),
(UUID(), '新竹縣', '湖口鄉', '303', NOW(), NOW()),
(UUID(), '新竹縣', '新豐鄉', '304', NOW(), NOW()),
(UUID(), '新竹縣', '新埔鎮', '305', NOW(), NOW()),
(UUID(), '新竹縣', '關西鎮', '306', NOW(), NOW()),
(UUID(), '新竹縣', '芎林鄉', '307', NOW(), NOW()),
(UUID(), '新竹縣', '竹東鎮', '310', NOW(), NOW()),
(UUID(), '新竹縣', '五峰鄉', '311', NOW(), NOW()),
(UUID(), '新竹縣', '橫山鄉', '312', NOW(), NOW()),
(UUID(), '新竹縣', '尖石鄉', '313', NOW(), NOW()),
(UUID(), '新竹縣', '北埔鄉', '314', NOW(), NOW()),
(UUID(), '新竹縣', '峨眉鄉', '315', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 8. 桃園市（13 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '桃園市', '中壢區', '320', NOW(), NOW()),
(UUID(), '桃園市', '平鎮區', '324', NOW(), NOW()),
(UUID(), '桃園市', '龍潭區', '325', NOW(), NOW()),
(UUID(), '桃園市', '楊梅區', '326', NOW(), NOW()),
(UUID(), '桃園市', '新屋區', '327', NOW(), NOW()),
(UUID(), '桃園市', '觀音區', '328', NOW(), NOW()),
(UUID(), '桃園市', '桃園區', '330', NOW(), NOW()),
(UUID(), '桃園市', '龜山區', '333', NOW(), NOW()),
(UUID(), '桃園市', '八德區', '334', NOW(), NOW()),
(UUID(), '桃園市', '大溪區', '335', NOW(), NOW()),
(UUID(), '桃園市', '復興區', '336', NOW(), NOW()),
(UUID(), '桃園市', '大園區', '337', NOW(), NOW()),
(UUID(), '桃園市', '蘆竹區', '338', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 9. 苗栗縣（18 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '苗栗縣', '竹南鎮', '350', NOW(), NOW()),
(UUID(), '苗栗縣', '頭份市', '351', NOW(), NOW()),
(UUID(), '苗栗縣', '三灣鄉', '352', NOW(), NOW()),
(UUID(), '苗栗縣', '南庄鄉', '353', NOW(), NOW()),
(UUID(), '苗栗縣', '獅潭鄉', '354', NOW(), NOW()),
(UUID(), '苗栗縣', '後龍鎮', '356', NOW(), NOW()),
(UUID(), '苗栗縣', '通霄鎮', '357', NOW(), NOW()),
(UUID(), '苗栗縣', '苑裡鎮', '358', NOW(), NOW()),
(UUID(), '苗栗縣', '苗栗市', '360', NOW(), NOW()),
(UUID(), '苗栗縣', '造橋鄉', '361', NOW(), NOW()),
(UUID(), '苗栗縣', '頭屋鄉', '362', NOW(), NOW()),
(UUID(), '苗栗縣', '公館鄉', '363', NOW(), NOW()),
(UUID(), '苗栗縣', '大湖鄉', '364', NOW(), NOW()),
(UUID(), '苗栗縣', '泰安鄉', '365', NOW(), NOW()),
(UUID(), '苗栗縣', '銅鑼鄉', '366', NOW(), NOW()),
(UUID(), '苗栗縣', '三義鄉', '367', NOW(), NOW()),
(UUID(), '苗栗縣', '西湖鄉', '368', NOW(), NOW()),
(UUID(), '苗栗縣', '卓蘭鎮', '369', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 10. 臺中市（29 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '臺中市', '中區', '400', NOW(), NOW()),
(UUID(), '臺中市', '東區', '401', NOW(), NOW()),
(UUID(), '臺中市', '南區', '402', NOW(), NOW()),
(UUID(), '臺中市', '西區', '403', NOW(), NOW()),
(UUID(), '臺中市', '北區', '404', NOW(), NOW()),
(UUID(), '臺中市', '北屯區', '406', NOW(), NOW()),
(UUID(), '臺中市', '西屯區', '407', NOW(), NOW()),
(UUID(), '臺中市', '南屯區', '408', NOW(), NOW()),
(UUID(), '臺中市', '太平區', '411', NOW(), NOW()),
(UUID(), '臺中市', '大里區', '412', NOW(), NOW()),
(UUID(), '臺中市', '霧峰區', '413', NOW(), NOW()),
(UUID(), '臺中市', '烏日區', '414', NOW(), NOW()),
(UUID(), '臺中市', '豐原區', '420', NOW(), NOW()),
(UUID(), '臺中市', '后里區', '421', NOW(), NOW()),
(UUID(), '臺中市', '石岡區', '422', NOW(), NOW()),
(UUID(), '臺中市', '東勢區', '423', NOW(), NOW()),
(UUID(), '臺中市', '和平區', '424', NOW(), NOW()),
(UUID(), '臺中市', '新社區', '426', NOW(), NOW()),
(UUID(), '臺中市', '潭子區', '427', NOW(), NOW()),
(UUID(), '臺中市', '大雅區', '428', NOW(), NOW()),
(UUID(), '臺中市', '神岡區', '429', NOW(), NOW()),
(UUID(), '臺中市', '大肚區', '432', NOW(), NOW()),
(UUID(), '臺中市', '沙鹿區', '433', NOW(), NOW()),
(UUID(), '臺中市', '龍井區', '434', NOW(), NOW()),
(UUID(), '臺中市', '梧棲區', '435', NOW(), NOW()),
(UUID(), '臺中市', '清水區', '436', NOW(), NOW()),
(UUID(), '臺中市', '大甲區', '437', NOW(), NOW()),
(UUID(), '臺中市', '外埔區', '438', NOW(), NOW()),
(UUID(), '臺中市', '大安區', '439', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 11. 彰化縣（26 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '彰化縣', '彰化市', '500', NOW(), NOW()),
(UUID(), '彰化縣', '芬園鄉', '502', NOW(), NOW()),
(UUID(), '彰化縣', '花壇鄉', '503', NOW(), NOW()),
(UUID(), '彰化縣', '秀水鄉', '504', NOW(), NOW()),
(UUID(), '彰化縣', '鹿港鎮', '505', NOW(), NOW()),
(UUID(), '彰化縣', '福興鄉', '506', NOW(), NOW()),
(UUID(), '彰化縣', '線西鄉', '507', NOW(), NOW()),
(UUID(), '彰化縣', '和美鎮', '508', NOW(), NOW()),
(UUID(), '彰化縣', '伸港鄉', '509', NOW(), NOW()),
(UUID(), '彰化縣', '員林市', '510', NOW(), NOW()),
(UUID(), '彰化縣', '社頭鄉', '511', NOW(), NOW()),
(UUID(), '彰化縣', '永靖鄉', '512', NOW(), NOW()),
(UUID(), '彰化縣', '埔心鄉', '513', NOW(), NOW()),
(UUID(), '彰化縣', '溪湖鎮', '514', NOW(), NOW()),
(UUID(), '彰化縣', '大村鄉', '515', NOW(), NOW()),
(UUID(), '彰化縣', '埔鹽鄉', '516', NOW(), NOW()),
(UUID(), '彰化縣', '田中鎮', '520', NOW(), NOW()),
(UUID(), '彰化縣', '北斗鎮', '521', NOW(), NOW()),
(UUID(), '彰化縣', '田尾鄉', '522', NOW(), NOW()),
(UUID(), '彰化縣', '埤頭鄉', '523', NOW(), NOW()),
(UUID(), '彰化縣', '溪州鄉', '524', NOW(), NOW()),
(UUID(), '彰化縣', '竹塘鄉', '525', NOW(), NOW()),
(UUID(), '彰化縣', '二林鎮', '526', NOW(), NOW()),
(UUID(), '彰化縣', '大城鄉', '527', NOW(), NOW()),
(UUID(), '彰化縣', '芳苑鄉', '528', NOW(), NOW()),
(UUID(), '彰化縣', '二水鄉', '530', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 12. 南投縣（13 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '南投縣', '南投市', '540', NOW(), NOW()),
(UUID(), '南投縣', '中寮鄉', '541', NOW(), NOW()),
(UUID(), '南投縣', '草屯鎮', '542', NOW(), NOW()),
(UUID(), '南投縣', '國姓鄉', '544', NOW(), NOW()),
(UUID(), '南投縣', '埔里鎮', '545', NOW(), NOW()),
(UUID(), '南投縣', '仁愛鄉', '546', NOW(), NOW()),
(UUID(), '南投縣', '名間鄉', '551', NOW(), NOW()),
(UUID(), '南投縣', '集集鎮', '552', NOW(), NOW()),
(UUID(), '南投縣', '水里鄉', '553', NOW(), NOW()),
(UUID(), '南投縣', '魚池鄉', '555', NOW(), NOW()),
(UUID(), '南投縣', '信義鄉', '556', NOW(), NOW()),
(UUID(), '南投縣', '竹山鎮', '557', NOW(), NOW()),
(UUID(), '南投縣', '鹿谷鄉', '558', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 13. 嘉義市（2 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '嘉義市', '西區', '600', NOW(), NOW()),
(UUID(), '嘉義市', '東區', '600', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 14. 嘉義縣（18 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '嘉義縣', '番路鄉', '602', NOW(), NOW()),
(UUID(), '嘉義縣', '梅山鄉', '603', NOW(), NOW()),
(UUID(), '嘉義縣', '竹崎鄉', '604', NOW(), NOW()),
(UUID(), '嘉義縣', '阿里山鄉', '605', NOW(), NOW()),
(UUID(), '嘉義縣', '中埔鄉', '606', NOW(), NOW()),
(UUID(), '嘉義縣', '大埔鄉', '607', NOW(), NOW()),
(UUID(), '嘉義縣', '水上鄉', '608', NOW(), NOW()),
(UUID(), '嘉義縣', '鹿草鄉', '611', NOW(), NOW()),
(UUID(), '嘉義縣', '太保市', '612', NOW(), NOW()),
(UUID(), '嘉義縣', '朴子市', '613', NOW(), NOW()),
(UUID(), '嘉義縣', '東石鄉', '614', NOW(), NOW()),
(UUID(), '嘉義縣', '六腳鄉', '615', NOW(), NOW()),
(UUID(), '嘉義縣', '新港鄉', '616', NOW(), NOW()),
(UUID(), '嘉義縣', '民雄鄉', '621', NOW(), NOW()),
(UUID(), '嘉義縣', '大林鎮', '622', NOW(), NOW()),
(UUID(), '嘉義縣', '溪口鄉', '623', NOW(), NOW()),
(UUID(), '嘉義縣', '義竹鄉', '624', NOW(), NOW()),
(UUID(), '嘉義縣', '布袋鎮', '625', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 15. 雲林縣（20 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '雲林縣', '斗南鎮', '630', NOW(), NOW()),
(UUID(), '雲林縣', '大埤鄉', '631', NOW(), NOW()),
(UUID(), '雲林縣', '虎尾鎮', '632', NOW(), NOW()),
(UUID(), '雲林縣', '土庫鎮', '633', NOW(), NOW()),
(UUID(), '雲林縣', '褒忠鄉', '634', NOW(), NOW()),
(UUID(), '雲林縣', '東勢鄉', '635', NOW(), NOW()),
(UUID(), '雲林縣', '臺西鄉', '636', NOW(), NOW()),
(UUID(), '雲林縣', '崙背鄉', '637', NOW(), NOW()),
(UUID(), '雲林縣', '麥寮鄉', '638', NOW(), NOW()),
(UUID(), '雲林縣', '斗六市', '640', NOW(), NOW()),
(UUID(), '雲林縣', '林內鄉', '643', NOW(), NOW()),
(UUID(), '雲林縣', '古坑鄉', '646', NOW(), NOW()),
(UUID(), '雲林縣', '莿桐鄉', '647', NOW(), NOW()),
(UUID(), '雲林縣', '西螺鎮', '648', NOW(), NOW()),
(UUID(), '雲林縣', '二崙鄉', '649', NOW(), NOW()),
(UUID(), '雲林縣', '北港鎮', '651', NOW(), NOW()),
(UUID(), '雲林縣', '水林鄉', '652', NOW(), NOW()),
(UUID(), '雲林縣', '口湖鄉', '653', NOW(), NOW()),
(UUID(), '雲林縣', '四湖鄉', '654', NOW(), NOW()),
(UUID(), '雲林縣', '元長鄉', '655', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 16. 臺南市（37 個行政區）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '臺南市', '中西區', '700', NOW(), NOW()),
(UUID(), '臺南市', '東區', '701', NOW(), NOW()),
(UUID(), '臺南市', '南區', '702', NOW(), NOW()),
(UUID(), '臺南市', '北區', '704', NOW(), NOW()),
(UUID(), '臺南市', '安平區', '708', NOW(), NOW()),
(UUID(), '臺南市', '安南區', '709', NOW(), NOW()),
(UUID(), '臺南市', '永康區', '710', NOW(), NOW()),
(UUID(), '臺南市', '歸仁區', '711', NOW(), NOW()),
(UUID(), '臺南市', '新化區', '712', NOW(), NOW()),
(UUID(), '臺南市', '左鎮區', '713', NOW(), NOW()),
(UUID(), '臺南市', '玉井區', '714', NOW(), NOW()),
(UUID(), '臺南市', '楠西區', '715', NOW(), NOW()),
(UUID(), '臺南市', '南化區', '716', NOW(), NOW()),
(UUID(), '臺南市', '仁德區', '717', NOW(), NOW()),
(UUID(), '臺南市', '關廟區', '718', NOW(), NOW()),
(UUID(), '臺南市', '龍崎區', '719', NOW(), NOW()),
(UUID(), '臺南市', '官田區', '720', NOW(), NOW()),
(UUID(), '臺南市', '麻豆區', '721', NOW(), NOW()),
(UUID(), '臺南市', '佳里區', '722', NOW(), NOW()),
(UUID(), '臺南市', '西港區', '723', NOW(), NOW()),
(UUID(), '臺南市', '七股區', '724', NOW(), NOW()),
(UUID(), '臺南市', '將軍區', '725', NOW(), NOW()),
(UUID(), '臺南市', '學甲區', '726', NOW(), NOW()),
(UUID(), '臺南市', '北門區', '727', NOW(), NOW()),
(UUID(), '臺南市', '新營區', '730', NOW(), NOW()),
(UUID(), '臺南市', '後壁區', '731', NOW(), NOW()),
(UUID(), '臺南市', '白河區', '732', NOW(), NOW()),
(UUID(), '臺南市', '東山區', '733', NOW(), NOW()),
(UUID(), '臺南市', '六甲區', '734', NOW(), NOW()),
(UUID(), '臺南市', '下營區', '735', NOW(), NOW()),
(UUID(), '臺南市', '柳營區', '736', NOW(), NOW()),
(UUID(), '臺南市', '鹽水區', '737', NOW(), NOW()),
(UUID(), '臺南市', '善化區', '741', NOW(), NOW()),
(UUID(), '臺南市', '新市區', '744', NOW(), NOW()),
(UUID(), '臺南市', '大內區', '742', NOW(), NOW()),
(UUID(), '臺南市', '山上區', '743', NOW(), NOW()),
(UUID(), '臺南市', '安定區', '745', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 17. 高雄市（38 個行政區）
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

-- 18. 澎湖縣（6 個鄉市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '澎湖縣', '馬公市', '880', NOW(), NOW()),
(UUID(), '澎湖縣', '西嶼鄉', '881', NOW(), NOW()),
(UUID(), '澎湖縣', '望安鄉', '882', NOW(), NOW()),
(UUID(), '澎湖縣', '七美鄉', '883', NOW(), NOW()),
(UUID(), '澎湖縣', '白沙鄉', '884', NOW(), NOW()),
(UUID(), '澎湖縣', '湖西鄉', '885', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 19. 金門縣（6 個鄉鎮）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '金門縣', '金沙鎮', '890', NOW(), NOW()),
(UUID(), '金門縣', '金湖鎮', '891', NOW(), NOW()),
(UUID(), '金門縣', '金寧鄉', '892', NOW(), NOW()),
(UUID(), '金門縣', '金城鎮', '893', NOW(), NOW()),
(UUID(), '金門縣', '烈嶼鄉', '894', NOW(), NOW()),
(UUID(), '金門縣', '烏坵鄉', '896', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 20. 屏東縣（33 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '屏東縣', '屏東市', '900', NOW(), NOW()),
(UUID(), '屏東縣', '三地門鄉', '901', NOW(), NOW()),
(UUID(), '屏東縣', '霧臺鄉', '902', NOW(), NOW()),
(UUID(), '屏東縣', '瑪家鄉', '903', NOW(), NOW()),
(UUID(), '屏東縣', '九如鄉', '904', NOW(), NOW()),
(UUID(), '屏東縣', '里港鄉', '905', NOW(), NOW()),
(UUID(), '屏東縣', '高樹鄉', '906', NOW(), NOW()),
(UUID(), '屏東縣', '鹽埔鄉', '907', NOW(), NOW()),
(UUID(), '屏東縣', '長治鄉', '908', NOW(), NOW()),
(UUID(), '屏東縣', '麟洛鄉', '909', NOW(), NOW()),
(UUID(), '屏東縣', '竹田鄉', '911', NOW(), NOW()),
(UUID(), '屏東縣', '內埔鄉', '912', NOW(), NOW()),
(UUID(), '屏東縣', '萬丹鄉', '913', NOW(), NOW()),
(UUID(), '屏東縣', '潮州鎮', '920', NOW(), NOW()),
(UUID(), '屏東縣', '泰武鄉', '921', NOW(), NOW()),
(UUID(), '屏東縣', '來義鄉', '922', NOW(), NOW()),
(UUID(), '屏東縣', '萬巒鄉', '923', NOW(), NOW()),
(UUID(), '屏東縣', '崁頂鄉', '924', NOW(), NOW()),
(UUID(), '屏東縣', '新埤鄉', '925', NOW(), NOW()),
(UUID(), '屏東縣', '南州鄉', '926', NOW(), NOW()),
(UUID(), '屏東縣', '林邊鄉', '927', NOW(), NOW()),
(UUID(), '屏東縣', '東港鎮', '928', NOW(), NOW()),
(UUID(), '屏東縣', '琉球鄉', '929', NOW(), NOW()),
(UUID(), '屏東縣', '佳冬鄉', '931', NOW(), NOW()),
(UUID(), '屏東縣', '新園鄉', '932', NOW(), NOW()),
(UUID(), '屏東縣', '枋寮鄉', '940', NOW(), NOW()),
(UUID(), '屏東縣', '枋山鄉', '941', NOW(), NOW()),
(UUID(), '屏東縣', '春日鄉', '942', NOW(), NOW()),
(UUID(), '屏東縣', '獅子鄉', '943', NOW(), NOW()),
(UUID(), '屏東縣', '車城鄉', '944', NOW(), NOW()),
(UUID(), '屏東縣', '牡丹鄉', '945', NOW(), NOW()),
(UUID(), '屏東縣', '恆春鎮', '946', NOW(), NOW()),
(UUID(), '屏東縣', '滿州鄉', '947', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 21. 臺東縣（16 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '臺東縣', '臺東市', '950', NOW(), NOW()),
(UUID(), '臺東縣', '綠島鄉', '951', NOW(), NOW()),
(UUID(), '臺東縣', '蘭嶼鄉', '952', NOW(), NOW()),
(UUID(), '臺東縣', '延平鄉', '953', NOW(), NOW()),
(UUID(), '臺東縣', '卑南鄉', '954', NOW(), NOW()),
(UUID(), '臺東縣', '鹿野鄉', '955', NOW(), NOW()),
(UUID(), '臺東縣', '關山鎮', '956', NOW(), NOW()),
(UUID(), '臺東縣', '海端鄉', '957', NOW(), NOW()),
(UUID(), '臺東縣', '池上鄉', '958', NOW(), NOW()),
(UUID(), '臺東縣', '東河鄉', '959', NOW(), NOW()),
(UUID(), '臺東縣', '成功鎮', '961', NOW(), NOW()),
(UUID(), '臺東縣', '長濱鄉', '962', NOW(), NOW()),
(UUID(), '臺東縣', '太麻里鄉', '963', NOW(), NOW()),
(UUID(), '臺東縣', '金峰鄉', '964', NOW(), NOW()),
(UUID(), '臺東縣', '大武鄉', '965', NOW(), NOW()),
(UUID(), '臺東縣', '達仁鄉', '966', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 22. 花蓮縣（13 個鄉鎮市）
INSERT INTO district (id, city, district, zip_code, created_at, updated_at) VALUES
(UUID(), '花蓮縣', '花蓮市', '970', NOW(), NOW()),
(UUID(), '花蓮縣', '新城鄉', '971', NOW(), NOW()),
(UUID(), '花蓮縣', '秀林鄉', '972', NOW(), NOW()),
(UUID(), '花蓮縣', '吉安鄉', '973', NOW(), NOW()),
(UUID(), '花蓮縣', '壽豐鄉', '974', NOW(), NOW()),
(UUID(), '花蓮縣', '鳳林鎮', '975', NOW(), NOW()),
(UUID(), '花蓮縣', '光復鄉', '976', NOW(), NOW()),
(UUID(), '花蓮縣', '豐濱鄉', '977', NOW(), NOW()),
(UUID(), '花蓮縣', '瑞穗鄉', '978', NOW(), NOW()),
(UUID(), '花蓮縣', '萬榮鄉', '979', NOW(), NOW()),
(UUID(), '花蓮縣', '玉里鎮', '981', NOW(), NOW()),
(UUID(), '花蓮縣', '卓溪鄉', '982', NOW(), NOW()),
(UUID(), '花蓮縣', '富里鄉', '983', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ========================================
-- 資料統計
-- ========================================

-- 檢查資料筆數
SELECT 
    '總計' AS category,
    COUNT(*) AS count 
FROM district
UNION ALL
SELECT 
    city AS category,
    COUNT(*) AS count 
FROM district 
GROUP BY city 
ORDER BY 
    CASE WHEN category = '總計' THEN 0 ELSE 1 END,
    category;

-- 列出所有縣市
SELECT DISTINCT city FROM district ORDER BY city;

SELECT '========================================' AS '';
SELECT '台灣行政區資料初始化完成！' AS '';
SELECT CONCAT('總計：', COUNT(*), ' 筆資料') AS '' FROM district;
SELECT '========================================' AS '';
