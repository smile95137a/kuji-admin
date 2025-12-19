package com.group.admin;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis Generator 自動生成程式
 * 
 * 功能說明：
 * - 自動掃描 kuji schema 的所有資料表
 * - 生成 entity、example、mapper、XML
 * - 不生成 WithBLOBs 類別（所有 BLOB 欄位合併至主 Entity）
 * - VARCHAR/UUID 主鍵對應 String（不轉換成 Long）
 * - UUID 主鍵由程式碼生成，不使用 generatedKey
 * 
 * 型別對應：
 * - varchar → String
 * - int → Integer
 * - datetime → LocalDateTime
 * - decimal → BigDecimal
 * - text/longtext → String（不生成 WithBLOBs）
 */
public class MBGAutoRunner {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/kuji?useSSL=false&serverTimezone=Asia/Taipei";
        String user = "root";
        String password = "123456";
        String schema = "kuji";
        String targetPackageEntity = "com.group.admin.entity";
        String targetPackageExample = "com.group.admin.example";
        String targetPackageMapper = "com.group.admin.mapper";
        String targetPackageXml = "mapper";

        List<String> tableConfigs = new ArrayList<>();

        // 取得 kuji schema 的 table
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(schema, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                // 只生成普通 table，不用 WithBLOBs，也不加 <generatedKey>
                // modelType="flat" 確保所有欄位（包含 BLOB）都在同一個 Entity 類別中
                String tableXml = String.format(
                        "        <table tableName=\"%s\" domainObjectName=\"%s\" " +
                                "enableCountByExample=\"true\" " +
                                "enableUpdateByExample=\"true\" " +
                                "enableDeleteByExample=\"true\" " +
                                "enableSelectByExample=\"true\" " +
                                "selectByExampleQueryId=\"true\" " +
                                "modelType=\"flat\">\n" +
                                "            <!-- 不使用 generatedKey，UUID 主鍵由程式碼生成 -->\n" +
                                "        </table>",
                        tableName, toPascalCase(tableName));
                tableConfigs.add(tableXml);
            }
        }

        // 將 & 轉成 &amp; 避免 XML 解析錯誤
        String safeUrl = url.replace("&", "&amp;");

        // 生成 generatorConfig.xml
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE generatorConfiguration PUBLIC \"-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN\" "
                + "\"http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd\">\n"
                + "<generatorConfiguration>\n"
                + "    <context id=\"MySQLTables\" targetRuntime=\"MyBatis3\">\n"
                + "\n"
                + "        <!-- 註解生成器：不生成日期與註解 -->\n"
                + "        <commentGenerator>\n"
                + "            <property name=\"suppressDate\" value=\"true\" />\n"
                + "            <property name=\"suppressAllComments\" value=\"true\" />\n"
                + "        </commentGenerator>\n"
                + "\n"
                + "        <!-- JDBC 連線設定 -->\n"
                + "        <jdbcConnection driverClass=\"com.mysql.cj.jdbc.Driver\"\n"
                + "            connectionURL=\"" + safeUrl + "\"\n"
                + "            userId=\"" + user + "\"\n"
                + "            password=\"" + password + "\">\n"
                + "            <!-- 避免讀取到其他 schema 的同名表格 -->\n"
                + "            <property name=\"nullCatalogMeansCurrent\" value=\"true\" />\n"
                + "        </jdbcConnection>\n"
                + "\n"
                + "        <!-- Java 型別解析器 -->\n"
                + "        <javaTypeResolver>\n"
                + "            <!-- 小數不強制使用 BigDecimal -->\n"
                + "            <property name=\"forceBigDecimals\" value=\"false\" />\n"
                + "            <!-- 使用 JSR310 日期時間型別（LocalDateTime 等）-->\n"
                + "            <property name=\"useJSR310Types\" value=\"true\" />\n"
                + "            <!-- LONGVARBINARY 不對應 Blob -->\n"
                + "            <property name=\"mapLongVarbinaryAsBlob\" value=\"false\" />\n"
                + "            <!-- BLOB 不對應 byte[] -->\n"
                + "            <property name=\"mapBlobAsBytes\" value=\"false\" />\n"
                + "        </javaTypeResolver>\n"
                + "\n"
                + "        <!-- Entity 與 Example 生成設定 -->\n"
                + "        <javaModelGenerator targetPackage=\"" + targetPackageEntity
                + "\" targetProject=\"src/main/java\">\n"
                + "            <property name=\"enableSubPackages\" value=\"false\" />\n"
                + "            <property name=\"trimStrings\" value=\"true\" />\n"
                + "            <property name=\"exampleTargetPackage\" value=\"" + targetPackageExample + "\" />\n"
                + "        </javaModelGenerator>\n"
                + "\n"
                + "        <!-- Mapper XML 生成設定 -->\n"
                + "        <sqlMapGenerator targetPackage=\"" + targetPackageXml
                + "\" targetProject=\"src/main/resources\">\n"
                + "            <property name=\"enableSubPackages\" value=\"false\" />\n"
                + "        </sqlMapGenerator>\n"
                + "\n"
                + "        <!-- Mapper 介面生成設定 -->\n"
                + "        <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"" + targetPackageMapper
                + "\" targetProject=\"src/main/java\">\n"
                + "            <property name=\"enableSubPackages\" value=\"false\" />\n"
                + "        </javaClientGenerator>\n"
                + "\n"
                + "        <!-- 資料表設定 -->\n"
                + String.join("\n", tableConfigs) + "\n"
                + "\n"
                + "    </context>\n"
                + "</generatorConfiguration>";

        String configFile = "generatorConfig.xml";
        Files.write(Paths.get(configFile), xmlContent.getBytes());
        System.out.println("✅ generatorConfig.xml 已生成完成！");

        // 執行 MyBatis Generator
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(new File(configFile));
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);

        System.out.println("✅ MyBatis Generator 執行完成，entity、example、mapper、XML 已生成！");
        if (!warnings.isEmpty()) {
            System.out.println("⚠️ warnings:");
            warnings.forEach(System.out::println);
        }
    }

    // 將 table_name 轉成 PascalCase
    private static String toPascalCase(String tableName) {
        StringBuilder result = new StringBuilder();
        for (String part : tableName.split("_")) {
            if (part.length() > 0) {
                result.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }
}
