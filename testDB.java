import java.sql.*;
import util.DBContext;
public class testDB {
    public static void main(String[] args) throws Exception {
        Connection conn = DBContext.getConnection();
        ResultSet rs = conn.getMetaData().getColumns(null, null, "StaffSchedule", null);
        while (rs.next()) {
            System.out.println(rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME") + " - Nullable: " + rs.getString("IS_NULLABLE"));
        }
    }
}
