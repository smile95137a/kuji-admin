package com.group.admin.generator;

import java.io.*;
import java.sql.*;
import java.util.*;

public class FullSchemaExampleGenerator {

    private static final String URL = "jdbc:mysql://localhost:3306/kuji?useSSL=false&serverTimezone=Asia/Taipei";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";
    private static final String SCHEMA = "dream";

    private static final String ENTITY_DIR = "src/main/java/com/group/admin/entity/";
    private static final String MAPPER_DIR = "src/main/resources/mapper/";
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

        // 取得 DB 欄位
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet columns = metaData.getColumns(null, SCHEMA, tableName, "%");
        Map<String, String> dbColumns = new LinkedHashMap<>();
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String typeName = columns.getString("TYPE_NAME");
            dbColumns.put(toCamelCase(columnName, false), sqlTypeToJavaType(typeName));
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
            writer.write("package com.group.admin.entity.example;\n\n");
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
                    + "Map\" parameterType=\"com.group.admin.entity.example." + className + "Example\">\n");
            writer.write("    SELECT * FROM " + tableName + "\n");
            writer.write("    <where>\n");
            writer.write("      <foreach collection=\"oredCriteria\" item=\"criteria\" separator=\"or\">\n");
            writer.write("        <foreach collection=\"criteria.conditions.entrySet()\" item=\"entry\">\n");
            writer.write("          ${entry.key} = #{entry.value} AND\n");
            writer.write("        </foreach>\n");
            writer.write("      </foreach>\n");
            writer.write("    </where>\n");
            writer.write("  </select>\n");

            writer.write("</mapper>\n");
        }

        System.out.println("生成完成: " + className + " + Example + Mapper");
    }

    private static String sqlTypeToJavaType(String sqlType) {
        sqlType = sqlType.toUpperCase();
        return switch (sqlType) {
            case "VARCHAR", "CHAR", "TEXT" -> "String";
            case "INT", "INTEGER", "SMALLINT", "TINYINT" -> "Integer";
            case "BIGINT" -> "Long";
            case "DECIMAL", "NUMERIC" -> "java.math.BigDecimal";
            case "DATE", "DATETIME", "TIMESTAMP" -> "java.time.LocalDateTime";
            case "BIT" -> "Boolean";
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
