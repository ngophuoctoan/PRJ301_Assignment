import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateDate {
    public static void main(String[] args) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1433;databaseName=PRJ301_ASSIGNMENT_DENTAL_CLINIC;encrypt=false;trustServerCertificate=true;";
            Connection c = DriverManager.getConnection(url, "sa", "123");
            Statement s = c.createStatement();
            s.executeUpdate("UPDATE Appointment SET work_date = CAST(GETDATE() AS DATE)");
            System.out.println("Update success with sa:123");
        } catch(Exception e) {
            try {
                String url = "jdbc:sqlserver://localhost:1433;databaseName=PRJ301_ASSIGNMENT_DENTAL_CLINIC;encrypt=false;trustServerCertificate=true;";
                Connection c = DriverManager.getConnection(url, "sa", "68");
                Statement s = c.createStatement();
                s.executeUpdate("UPDATE Appointment SET work_date = CAST(GETDATE() AS DATE)");
                System.out.println("Update success with sa:68");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
