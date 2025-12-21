import java.sql.*;

public class check_db_schema {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com:3306/onekuji?serverTimezone=UTC";
        String user = "admin";
        String password = "EASONlotery!!";
        
        Connection conn = DriverManager.getConnection(url, user, password);
        DatabaseMetaData metaData = conn.getMetaData();
        
        System.out.println("=== role 表格結構 ===");
        ResultSet columns = metaData.getColumns(null, "onekuji", "role", "%");
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String typeName = columns.getString("TYPE_NAME");
            int size = columns.getInt("COLUMN_SIZE");
            System.out.println(columnName + " " + typeName + "(" + size + ")");
        }
        
        conn.close();
    }
}
