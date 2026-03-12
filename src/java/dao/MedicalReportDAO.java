package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.MedicalReport;
import model.Prescription;
import util.DBContext;

/**
 * MedicalReportDAO — DAO duy nhất quản lý toàn bộ logic liên quan tới
 * MedicalReport và Prescription.
 *
 * Thay thế logic phân tán trước đây trong DoctorDAO và MedicineDAO.
 *
 * @author Refactored
 */
public class MedicalReportDAO {

    // =========================================================================
    // WRITE METHODS
    // =========================================================================

    /**
     * Thêm mới hồ sơ bệnh án. Trả về report_id vừa tạo, hoặc -1 nếu lỗi.
     * Overload không có reexamLan2 (mặc định false).
     */
    public static int insertMedicalReport(int appointmentId, long doctorId,
            int patientId, String diagnosis, String treatmentPlan,
            String note, String sign) throws SQLException {
        return insertMedicalReport(appointmentId, doctorId, patientId,
                diagnosis, treatmentPlan, note, sign, false);
    }

    /**
     * Thêm mới hồ sơ bệnh án (phiên bản đầy đủ, hỗ trợ tái khám lần 2).
     * Trả về report_id vừa tạo, hoặc -1 nếu lỗi.
     */
    public static int insertMedicalReport(int appointmentId, long doctorId,
            int patientId, String diagnosis, String treatmentPlan,
            String note, String sign, boolean isReexamLan2) throws SQLException {

        String sql = "INSERT INTO MedicalReport "
                + "(appointment_id, doctor_id, patient_id, diagnosis, "
                + "treatment_plan, note, sign, is_reexam_lan_2) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointmentId);
            ps.setLong(2, doctorId);
            ps.setInt(3, patientId);
            ps.setString(4, diagnosis);
            ps.setString(5, treatmentPlan);
            ps.setString(6, note);
            ps.setString(7, sign);
            ps.setBoolean(8, isReexamLan2);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Thêm một dòng đơn thuốc vào Prescription, gắn với report_id.
     */
    public static void insertPrescription(int reportId, int medicineId,
            int quantity, String usage) throws SQLException {

        String sql = "INSERT INTO Prescription (report_id, medicine_id, quantity, usage) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reportId);
            ps.setInt(2, medicineId);
            ps.setInt(3, quantity);
            ps.setString(4, usage);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật nội dung hồ sơ bệnh án (chẩn đoán, phác đồ, ghi chú, chữ ký).
     * Trả về true nếu cập nhật thành công.
     */
    public static boolean updateMedicalReport(int reportId, String diagnosis,
            String treatmentPlan, String note, String sign) throws SQLException {

        String sql = "UPDATE MedicalReport "
                + "SET diagnosis = ?, treatment_plan = ?, note = ?, sign = ? "
                + "WHERE report_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, diagnosis);
            ps.setString(2, treatmentPlan);
            ps.setString(3, note);
            ps.setString(4, sign);
            ps.setInt(5, reportId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa hồ sơ bệnh án và toàn bộ đơn thuốc liên quan (dùng transaction).
     * Trả về true nếu xóa thành công.
     */
    public static boolean deleteMedicalReport(int reportId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // Xóa prescriptions trước (FK constraint)
            String deletePrescSql = "DELETE FROM Prescription WHERE report_id = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(deletePrescSql)) {
                ps1.setInt(1, reportId);
                ps1.executeUpdate();
            }

            // Xóa medical report
            String deleteReportSql = "DELETE FROM MedicalReport WHERE report_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(deleteReportSql)) {
                ps2.setInt(1, reportId);
                int rows = ps2.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // =========================================================================
    // READ METHODS
    // =========================================================================

    /**
     * Lấy hồ sơ bệnh án theo report_id, JOIN tên bệnh nhân và bác sĩ.
     */
    public static MedicalReport getMedicalReportById(int reportId) {
        String sql = "SELECT mr.*, p.full_name AS patient_name, d.full_name AS doctor_name "
                + "FROM MedicalReport mr "
                + "JOIN Patients p ON mr.patient_id = p.patient_id "
                + "JOIN Doctors d  ON mr.doctor_id  = d.doctor_id "
                + "WHERE mr.report_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy hồ sơ bệnh án theo appointment_id, JOIN tên bệnh nhân và bác sĩ.
     * Dùng chung cho Doctor view và Patient view.
     */
    public static MedicalReport getMedicalReportByAppointmentId(int appointmentId) {
        String sql = "SELECT mr.*, p.full_name AS patient_name, d.full_name AS doctor_name "
                + "FROM MedicalReport mr "
                + "JOIN Patients p ON mr.patient_id = p.patient_id "
                + "JOIN Doctors d  ON mr.doctor_id  = d.doctor_id "
                + "WHERE mr.appointment_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBContext.getConnection();
            if (conn != null) {
                ps = conn.prepareStatement(sql);
                ps.setInt(1, appointmentId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return null;
    }

    /**
     * Lấy danh sách tất cả hồ sơ bệnh án của một bệnh nhân.
     */
    public static List<MedicalReport> getMedicalReportsByPatientId(int patientId) {
        List<MedicalReport> reports = new ArrayList<>();
        String sql = "SELECT mr.*, p.full_name AS patient_name, d.full_name AS doctor_name "
                + "FROM MedicalReport mr "
                + "JOIN Patients p ON mr.patient_id = p.patient_id "
                + "JOIN Doctors d  ON mr.doctor_id  = d.doctor_id "
                + "WHERE mr.patient_id = ? "
                + "ORDER BY mr.created_at DESC";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBContext.getConnection();
            if (conn != null) {
                ps = conn.prepareStatement(sql);
                ps.setInt(1, patientId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    reports.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return reports;
    }

    /**
     * Lấy danh sách toa thuốc theo report_id, JOIN tên thuốc từ bảng Medicine.
     */
    public static List<Prescription> getPrescriptionsByReportId(int reportId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.prescription_id, p.report_id, p.medicine_id, "
                + "m.name, p.quantity, p.usage "
                + "FROM Prescription p "
                + "JOIN Medicine m ON p.medicine_id = m.medicine_id "
                + "WHERE p.report_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBContext.getConnection();
            if (conn != null) {
                ps = conn.prepareStatement(sql);
                ps.setInt(1, reportId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Prescription pres = new Prescription();
                    pres.setPrescriptionId(rs.getInt("prescription_id"));
                    pres.setReportId(rs.getInt("report_id"));
                    pres.setMedicineId(rs.getInt("medicine_id"));
                    pres.setName(rs.getString("name"));
                    pres.setQuantity(rs.getInt("quantity"));
                    pres.setUsage(rs.getString("usage"));
                    list.add(pres);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return list;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Map một ResultSet row → MedicalReport object.
     * Yêu cầu ResultSet đã có các cột từ SELECT mr.*, patient_name, doctor_name.
     */
    private static MedicalReport mapRow(ResultSet rs) throws SQLException {
        MedicalReport report = new MedicalReport();
        report.setReportId(rs.getInt("report_id"));
        report.setAppointmentId(rs.getInt("appointment_id"));
        report.setDoctorId(rs.getInt("doctor_id"));
        report.setPatientId(rs.getInt("patient_id"));
        report.setDiagnosis(rs.getString("diagnosis"));
        report.setTreatmentPlan(rs.getString("treatment_plan"));
        report.setNote(rs.getString("note"));
        report.setCreatedAt(rs.getTimestamp("created_at"));
        report.setSign(rs.getString("sign"));
        // patient_name và doctor_name từ JOIN
        try { report.setPatientName(rs.getString("patient_name")); }
        catch (SQLException ignored) {}
        try { report.setDoctorName(rs.getString("doctor_name")); }
        catch (SQLException ignored) {}
        // is_reexam_lan_2 — optional column
        try { report.setReexamLan2(rs.getBoolean("is_reexam_lan_2")); }
        catch (SQLException ignored) { report.setReexamLan2(false); }
        return report;
    }

    /** Đóng ResultSet, PreparedStatement, Connection mà không ném exception. */
    private static void closeQuietly(ResultSet rs, PreparedStatement ps, Connection conn) {
        try { if (rs   != null) rs.close();   } catch (SQLException ignored) {}
        try { if (ps   != null) ps.close();   } catch (SQLException ignored) {}
        try { if (conn != null) conn.close();  } catch (SQLException ignored) {}
    }
}
