package com.group.admin.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 完整的抽獎流程整合測試
 * 
 * 測試流程：
 * 1. 登入取得 Token
 * 2. 建立商品與獎品
 * 3. 確認籤位是否生成
 * 4. 前台查詢商品
 * 5. 執行抽獎
 * 6. 測試保護時間機制
 * 7. 查詢賞品盒
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("需要真實資料庫，CI 跳過")
public class LotteryFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String adminToken;
    private String lotteryId;
    private String storeId;

    @BeforeAll
    void setup() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    @Test
    @Order(1)
    void step1_登入後台取得Token() throws Exception {
        System.out.println("========================================");
        System.out.println("[Step 1] 登入後台取得 Token...");
        System.out.println("========================================");

        String loginUrl = baseUrl + "/admin/auth/login";
        Map<String, String> loginBody = Map.of(
            "email", "admin@kuji.com",
            "password", "admin123"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, request, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(), "登入應該成功");
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        assertTrue(responseJson.has("data"), "應該有 data 欄位");
        assertTrue(responseJson.get("data").has("token"), "應該有 token");
        
        adminToken = responseJson.get("data").get("token").asText();
        assertNotNull(adminToken, "Token 不應為 null");
        
        System.out.println("✅ 登入成功！Token: " + adminToken.substring(0, Math.min(50, adminToken.length())) + "...");
    }

    @Test
    @Order(2)
    void step2_查詢或建立店家() throws Exception {
        System.out.println("\n========================================");
        System.out.println("[Step 2] 查詢店家列表...");
        System.out.println("========================================");

        String storeListUrl = baseUrl + "/admin/store/list";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        HttpEntity<String> request = new HttpEntity<>("{}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(storeListUrl, request, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        
        if (responseJson.has("data") && responseJson.get("data").isArray() && responseJson.get("data").size() > 0) {
            storeId = responseJson.get("data").get(0).get("id").asText();
            String storeName = responseJson.get("data").get(0).has("name") 
                ? responseJson.get("data").get(0).get("name").asText() 
                : "N/A";
            System.out.println("✅ 使用現有店家: " + storeId + " (" + storeName + ")");
        } else {
            // 建立新店家
            System.out.println("⚠️ 沒有店家，先建立一個...");
            String createStoreUrl = baseUrl + "/admin/store";
            String storeBody = """
                {
                    "name": "測試店家",
                    "description": "用於測試的店家",
                    "status": "ACTIVE"
                }
                """;
            HttpEntity<String> createRequest = new HttpEntity<>(storeBody, headers);
            ResponseEntity<String> createResponse = restTemplate.postForEntity(createStoreUrl, createRequest, String.class);
            
            JsonNode createJson = objectMapper.readTree(createResponse.getBody());
            storeId = createJson.get("data").get("id").asText();
            System.out.println("✅ 店家建立成功: " + storeId);
        }
        
        assertNotNull(storeId, "Store ID 不應為 null");
    }

    @Test
    @Order(3)
    void step3_建立商品與獎品() throws Exception {
        System.out.println("\n========================================");
        System.out.println("[Step 3] 建立商品與獎品...");
        System.out.println("========================================");

        String createUrl = baseUrl + "/admin/lottery/with-prizes";
        
        String body = """
            {
                "lottery": {
                    "storeId": "%s",
                    "title": "測試一番賞_%d",
                    "description": "測試用的一番賞商品",
                    "category": "CUSTOM_LOTTERY",
                    "subCategory": "LOTTERY_MODE",
                    "pricePerDraw": 100,
                    "maxDraws": 10,
                    "status": "ON_SHELF"
                },
                "prizes": [
                    {
                        "name": "A賞 - 限定公仔",
                        "level": "A",
                        "quantity": 1,
                        "isGrandPrize": true
                    },
                    {
                        "name": "B賞 - 精美掛畫",
                        "level": "B",
                        "quantity": 2,
                        "isGrandPrize": false
                    },
                    {
                        "name": "C賞 - 鑰匙圈",
                        "level": "C",
                        "quantity": 3,
                        "isGrandPrize": false
                    },
                    {
                        "name": "D賞 - 小公仔",
                        "level": "D",
                        "quantity": 4,
                        "isGrandPrize": false
                    }
                ]
            }
            """.formatted(storeId, System.currentTimeMillis());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(createUrl, request, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(), "建立商品應該成功");
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        assertTrue(responseJson.has("data"), "應該有 data 欄位");
        
        JsonNode data = responseJson.get("data");
        assertTrue(data.has("lottery"), "應該有 lottery 欄位");
        
        lotteryId = data.get("lottery").get("id").asText();
        String title = data.get("lottery").get("title").asText();
        int prizeCount = data.has("prizes") ? data.get("prizes").size() : 0;
        
        System.out.println("✅ 商品建立成功: " + lotteryId + " (" + title + ")");
        System.out.println("   獎品數量: " + prizeCount);
        
        assertNotNull(lotteryId, "Lottery ID 不應為 null");
    }

    @Test
    @Order(4)
    void step4_檢查籤位是否生成() throws Exception {
        System.out.println("\n========================================");
        System.out.println("[Step 4] 檢查籤位是否生成...");
        System.out.println("========================================");

        String ticketsUrl = baseUrl + "/lottery/draw/" + lotteryId + "/tickets";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(ticketsUrl, HttpMethod.GET, request, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(), "查詢籤位應該成功");
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        
        // 檢查 data 或直接的 tickets
        JsonNode ticketsNode = responseJson.has("data") && responseJson.get("data").has("tickets")
            ? responseJson.get("data").get("tickets")
            : (responseJson.has("tickets") ? responseJson.get("tickets") : null);
        
        if (ticketsNode == null || !ticketsNode.isArray()) {
            System.out.println("❌ 無法找到籤位資料");
            System.out.println("   完整回應: " + response.getBody());
            fail("無法找到籤位資料");
            return;
        }
        
        int ticketCount = ticketsNode.size();
        System.out.println("✅ 籤位數量: " + ticketCount);
        
        if (ticketCount == 0) {
            System.out.println("❌ 籤位未生成！這是一個 BUG");
            fail("籤位未生成");
            return;
        }
        
        // 顯示前 5 個籤位
        System.out.println("   前 5 個籤位:");
        for (int i = 0; i < Math.min(5, ticketCount); i++) {
            JsonNode ticket = ticketsNode.get(i);
            int ticketNumber = ticket.has("ticketNumber") ? ticket.get("ticketNumber").asInt() : -1;
            String status = ticket.has("status") ? ticket.get("status").asText() : "N/A";
            System.out.println("   - 籤位 " + ticketNumber + ": " + status);
        }
        
        assertEquals(10, ticketCount, "應該有 10 個籤位 (maxDraws=10)");
    }

    @Test
    @Order(5)
    void step5_前台查詢商品列表() throws Exception {
        System.out.println("\n========================================");
        System.out.println("[Step 5] 前台查詢商品列表...");
        System.out.println("========================================");

        String listUrl = baseUrl + "/lottery/browse/list";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>("{}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(listUrl, request, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(), "前台查詢商品應該成功");
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode dataNode = responseJson.has("data") ? responseJson.get("data") : responseJson;
        
        int count = dataNode.isArray() ? dataNode.size() : 0;
        System.out.println("✅ 前台商品數量: " + count);
        
        assertTrue(count > 0, "應該至少有一個上架商品");
    }

    @Test
    @Order(6)
    void step6_前台查詢店家商品() throws Exception {
        System.out.println("\n========================================");
        System.out.println("[Step 6] 前台查詢店家商品...");
        System.out.println("========================================");

        String storeProductsUrl = baseUrl + "/lottery/browse/store/" + storeId;
        
        ResponseEntity<String> response = restTemplate.getForEntity(storeProductsUrl, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(), "前台查詢店家商品應該成功");
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode dataNode = responseJson.has("data") ? responseJson.get("data") : responseJson;
        
        int count = dataNode.isArray() ? dataNode.size() : 0;
        System.out.println("✅ 該店家商品數量: " + count);
    }

    @Test
    @Order(7)
    void step7_執行抽獎() throws Exception {
        System.out.println("\n========================================");
        System.out.println("[Step 7] 執行抽獎...");
        System.out.println("========================================");

        String drawUrl = baseUrl + "/lottery/draw/" + lotteryId + "/draw";
        
        String body = """
            {
                "ticketNumber": null,
                "drawCount": 1
            }
            """;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(drawUrl, request, String.class);
        
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(), "抽獎應該成功");
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode dataNode = responseJson.has("data") ? responseJson.get("data") : responseJson;
        
        if (dataNode.has("success") && dataNode.get("success").asBoolean()) {
            int ticketNumber = dataNode.has("ticketNumber") ? dataNode.get("ticketNumber").asInt() : -1;
            String prizeLevel = dataNode.has("prizeLevel") ? dataNode.get("prizeLevel").asText() : "N/A";
            String prizeName = dataNode.has("prizeName") ? dataNode.get("prizeName").asText() : "N/A";
            
            System.out.println("✅ 抽獎成功！");
            System.out.println("   籤位: " + ticketNumber);
            System.out.println("   獎品等級: " + prizeLevel);
            System.out.println("   獎品名稱: " + prizeName);
        } else {
            String message = dataNode.has("message") ? dataNode.get("message").asText() : "Unknown error";
            System.out.println("⚠️ 抽獎未成功: " + message);
        }
    }

    @Test
    @Order(8)
    void step8_再次查詢籤位確認抽獎結果() throws Exception {
        System.out.println("\n========================================");
        System.out.println("[Step 8] 再次查詢籤位確認抽獎結果...");
        System.out.println("========================================");

        String ticketsUrl = baseUrl + "/lottery/draw/" + lotteryId + "/tickets";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(ticketsUrl, HttpMethod.GET, request, String.class);
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode ticketsNode = responseJson.has("data") && responseJson.get("data").has("tickets")
            ? responseJson.get("data").get("tickets")
            : (responseJson.has("tickets") ? responseJson.get("tickets") : null);
        
        if (ticketsNode != null && ticketsNode.isArray()) {
            int availableCount = 0;
            int drawnCount = 0;
            
            for (JsonNode ticket : ticketsNode) {
                String status = ticket.has("status") ? ticket.get("status").asText() : "N/A";
                if ("AVAILABLE".equals(status)) {
                    availableCount++;
                } else if ("DRAWN".equals(status)) {
                    drawnCount++;
                }
            }
            
            System.out.println("✅ 籤位狀態:");
            System.out.println("   可抽: " + availableCount);
            System.out.println("   已抽: " + drawnCount);
        }
    }
}
