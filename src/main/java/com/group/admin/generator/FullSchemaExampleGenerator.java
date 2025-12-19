package com.group.admin.generator;

import java.io.*;
import java.sql.*;
import java.util.*;

public class FullSchemaExampleGenerator {

    private static final String URL = "jdbc:mysql://localhost:3306/kuji?useSSL=false&serverTimezone=Asia/Taipei";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";
    private static final String SCHEMA = "kuji";  // ⚠️ 修正：與 URL 中的資料庫名稱一致

    private static final String ENTITY_DIR = "src/main/java/com/group/admin/entity/";
    private static final String MAPPER_DIR = "src/main/resources/mapper/";
    private static final String MAPPER_INTERFACE_DIR = "src/main/java/com/group/admin/mapper/";
    private static final String EXAMPLE_DIR = "src/main/java/com/group/admin/example/";

    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        DatabaseMetaData metaData = conn.getMetaData();

        ResultSet tables = metaData.getTables(null, SCHEMA, "%", new String[] { "TABLE" });
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            generateEntityMapperExample(conn, tableName);
        }

        conn.close();
        System.out.println("✅ 全部表格同步完成（含 Example）！");
    }

    private static void generateEntityMapperExample(Connection conn, String tableName) throws Exception {
        String className = toCamelCase(tableName, true);
        String entityFilePath = ENTITY_DIR + className + ".java";
        String exampleFilePath = EXAMPLE_DIR + className + "Example.java";
        String mapperFilePath = MAPPER_DIR + className + "Mapper.xml";
        String mapperInterfaceFilePath = MAPPER_INTERFACE_DIR + className + "Mapper.java";

        // 取得 DB 欄位
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet columns = metaData.getColumns(null, SCHEMA, tableName, "%");
        Map<String, String> dbColumns = new LinkedHashMap<>();
        String idFieldType = "String"; // 預設 ID 為 String (UUID)
        
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String typeName = columns.getString("TYPE_NAME");
            String javaType = sqlTypeToJavaType(typeName);
            String camelName = toCamelCase(columnName, false);
            dbColumns.put(camelName, javaType);
            
            // 記錄 ID 欄位的型別
            if ("id".equalsIgnoreCase(columnName)) {
                idFieldType = javaType;
            }
        }

        // -------- 生成 Entity --------
        new File(ENTITY_DIR).mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(entityFilePath))) {
            writer.write("package com.group.admin.entity;\n\n");
            writer.write("import lombok.Data;\n\n");
            writer.write("@Data\npublic class " + className + " {\n");
            for (Map.Entry<String, String> entry : dbColumns.entrySet()) {
                writer.write("    private " + entry.getValue() + " " + entry.getKey() + ";\n");
            }
            writer.write("}\n");
        }

        // -------- 生成 Example --------
        new File(EXAMPLE_DIR).mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFilePath))) {
            writer.write("package com.group.admin.example;\n\n");
            writer.write("import java.util.*;\n");
            writer.write("import com.group.admin.entity." + className + ";\n\n");
            writer.write("public class " + className + "Example {\n");
            writer.write("    private List<Criteria> oredCriteria = new ArrayList<>();\n\n");

            writer.write("    public static class Criteria {\n");
            writer.write("        private Map<String, Object> conditions = new LinkedHashMap<>();\n\n");
            for (String col : dbColumns.keySet()) {
                writer.write("        public Criteria and" + toCamelCase(col, true) + "EqualTo(" +
                        dbColumns.get(col) + " value) {\n");
                writer.write("            conditions.put(\"" + col + "\", value);\n");
                writer.write("            return this;\n        }\n");
            }
            writer.write("    }\n");

            writer.write("    public Criteria createCriteria() {\n");
            writer.write("        Criteria criteria = new Criteria();\n");
            writer.write("        oredCriteria.add(criteria);\n");
            writer.write("        return criteria;\n");
            writer.write("    }\n");
            writer.write("}\n");
        }

        // -------- 生成 Mapper XML --------
        new File(MAPPER_DIR).mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mapperFilePath))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" " +
                    "\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
            writer.write("<mapper namespace=\"com.group.admin.mapper." + className + "Mapper\">\n\n");

            writer.write(
                    "  <resultMap id=\"" + className + "Map\" type=\"com.group.admin.entity." + className + "\">\n");
            for (String col : dbColumns.keySet()) {
                writer.write("    <result column=\"" + col + "\" property=\"" + col + "\" />\n");
            }
            writer.write("  </resultMap>\n\n");

            // select by Example
            writer.write("  <select id=\"selectByExample\" resultMap=\"" + className
                    + "Map\" parameterType=\"com.group.admin.example." + className + "Example\">\n");
            writer.write("    SELECT * FROM " + tableName + "\n");
            writer.write("    <where>\n");
            writer.write("      <foreach collection=\"oredCriteria\" item=\"criteria\" separator=\"or\">\n");
            writer.write("        <if test=\"criteria.conditions.size() > 0\">\n");
            writer.write("          <trim prefix=\"(\" suffix=\")\" prefixOverrides=\"and\">\n");
            writer.write("            <foreach collection=\"criteria.conditions.entrySet()\" item=\"entry\" index=\"key\">\n");
            writer.write("              and ${key} = #{entry.value}\n");
            writer.write("            </foreach>\n");
            writer.write("          </trim>\n");
            writer.write("        </if>\n");
            writer.write("      </foreach>\n");
            writer.write("    </where>\n");
            writer.write("  </select>\n\n");

            // count by Example
            writer.write("  <select id=\"countByExample\" resultType=\"long\" parameterType=\"com.group.admin.example." + className + "Example\">\n");
            writer.write("    SELECT COUNT(*) FROM " + tableName + "\n");
            writer.write("    <where>\n");
            writer.write("      <foreach collection=\"oredCriteria\" item=\"criteria\" separator=\"or\">\n");
            writer.write("        <if test=\"criteria.conditions.size() > 0\">\n");
            writer.write("          <trim prefix=\"(\" suffix=\")\" prefixOverrides=\"and\">\n");
            writer.write("            <foreach collection=\"criteria.conditions.entrySet()\" item=\"entry\" index=\"key\">\n");
            writer.write("              and ${key} = #{entry.value}\n");
            writer.write("            </foreach>\n");
            writer.write("          </trim>\n");
            writer.write("        </if>\n");
            writer.write("      </foreach>\n");
            writer.write("    </where>\n");
            writer.write("  </select>\n\n");

            // delete by Example
            writer.write("  <delete id=\"deleteByExample\" parameterType=\"com.group.admin.example." + className + "Example\">\n");
            writer.write("    DELETE FROM " + tableName + "\n");
            writer.write("    <where>\n");
            writer.write("      <foreach collection=\"oredCriteria\" item=\"criteria\" separator=\"or\">\n");
            writer.write("        <if test=\"criteria.conditions.size() > 0\">\n");
            writer.write("          <trim prefix=\"(\" suffix=\")\" prefixOverrides=\"and\">\n");
            writer.write("            <foreach collection=\"criteria.conditions.entrySet()\" item=\"entry\" index=\"key\">\n");
            writer.write("              and ${key} = #{entry.value}\n");
            writer.write("            </foreach>\n");
            writer.write("          </trim>\n");
            writer.write("        </if>\n");
            writer.write("      </foreach>\n");
            writer.write("    </where>\n");
            writer.write("  </delete>\n\n");

            writer.write("</mapper>\n");
        }

        // -------- 生成 Mapper Interface --------
        new File(MAPPER_INTERFACE_DIR).mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mapperInterfaceFilePath))) {
            writer.write("package com.group.admin.mapper;\n\n");
            writer.write("import com.group.admin.entity." + className + ";\n");
            writer.write("import com.group.admin.example." + className + "Example;\n");
            writer.write("import org.apache.ibatis.annotations.Mapper;\n");
            writer.write("import org.apache.ibatis.annotations.Param;\n");
            writer.write("import java.util.List;\n\n");
            writer.write("@Mapper\n");
            writer.write("public interface " + className + "Mapper {\n\n");
            
            // 基本 CRUD 方法
            writer.write("    int deleteByPrimaryKey(@Param(\"id\") " + idFieldType + " id);\n\n");
            writer.write("    int insert(" + className + " row);\n\n");
            writer.write("    " + className + " selectByPrimaryKey(@Param(\"id\") " + idFieldType + " id);\n\n");
            writer.write("    List<" + className + "> selectAll();\n\n");
            writer.write("    int updateByPrimaryKey(" + className + " row);\n\n");
            
            // Example 相關方法
            writer.write("    List<" + className + "> selectByExample(" + className + "Example example);\n\n");
            writer.write("    long countByExample(" + className + "Example example);\n\n");
            writer.write("    int deleteByExample(" + className + "Example example);\n\n");
            
            writer.write("}\n");
        }

        System.out.println("生成完成: " + className + " (Entity + Example + Mapper XML + Mapper Interface)");
    }

    private static String sqlTypeToJavaType(String sqlType) {
        sqlType = sqlType.toUpperCase();
        return switch (sqlType) {
            case "VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT" -> "String";
            case "INT", "INTEGER", "SMALLINT", "TINYINT" -> "Integer";
            case "BIGINT" -> "Long";
            case "DECIMAL", "NUMERIC" -> "java.math.BigDecimal";
            case "DATE", "DATETIME", "TIMESTAMP" -> "java.time.LocalDateTime";
            case "BIT", "BOOLEAN" -> "Boolean";
            case "DOUBLE", "FLOAT" -> "Double";
            default -> "String";
        };
    }

    private static String toCamelCase(String s, boolean startWithUpper) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = startWithUpper;
        for (char c : s.toCharArray()) {
            if (c == '_' || c == '-') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upperNext = false;
            }
        }
        return sb.toString();
    }
}
