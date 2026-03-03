import java.sql.*;
import util.DBContext;
public class testInsert {
    public static void main(String[] args) throws Exception {
        Connection conn = DBContext.getConnection();
        String sql = "INSERT INTO StaffSchedule (staff_id, work_date, slot_id, status, created_at) VALUES (1, '2026-03-30', NULL, 'pending', GETDATE())";
        PreparedStatement ps = conn.prepareStatement(sql);
        int result = ps.executeUpdate();
        System.out.println("Result: " + result);
    }
}
