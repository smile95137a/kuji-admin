import java.sql.*;

public class check_local_tables {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/kuji?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "123456";
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, user, password);
        DatabaseMetaData metaData = conn.getMetaData();
        
        System.out.println("=== LOCAL 資料庫 kuji 的所有表格 ===");
        ResultSet tables = metaData.getTables(null, "kuji", "%", new String[] { "TABLE" });
        int count = 0;
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            System.out.println((++count) + ". " + tableName);
        }
        System.out.println("\n總共 " + count + " 個表格");
        
        conn.close();
    }
}
