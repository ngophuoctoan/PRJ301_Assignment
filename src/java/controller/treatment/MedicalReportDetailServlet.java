package controller.treatment;

import dao.MedicalReportDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.MedicalReport;
import model.Prescription;

/**
 * MedicalReportDetailServlet — Hiển thị hồ sơ bệnh án cho Patient.
 *
 * Đã được refactor:
 *   - MedicineDAO.getReportByAppointmentId()  → MedicalReportDAO.getMedicalReportByAppointmentId()
 *   - MedicineDAO.getPrescriptionsByReportId() → MedicalReportDAO.getPrescriptionsByReportId()
 *
 * @author Refactored
 */
public class MedicalReportDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String appointmentIdParam = request.getParameter("appointmentId");

        if (appointmentIdParam == null || appointmentIdParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu appointmentId");
            return;
        }

        try {
            int appointmentId = Integer.parseInt(appointmentIdParam);

            // ------------------------------------------------------------------
            // Tìm MedicalReport theo appointmentId — dùng MedicalReportDAO
            // ------------------------------------------------------------------
            MedicalReport report = MedicalReportDAO.getMedicalReportByAppointmentId(appointmentId);
            if (report == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy hồ sơ bệnh án cho lịch hẹn ID: " + appointmentId);
                return;
            }

            // ------------------------------------------------------------------
            // Tải toa thuốc — dùng MedicalReportDAO
            // ------------------------------------------------------------------
            List<Prescription> prescriptions = MedicalReportDAO.getPrescriptionsByReportId(report.getReportId());

            request.setAttribute("report",        report);
            request.setAttribute("prescriptions", prescriptions);

            request.getRequestDispatcher("/view/jsp/patient/user_medicalreport.jsp")
                   .forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "appointmentId không hợp lệ");
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace(out);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "MedicalReportDetailServlet — Bệnh nhân xem hồ sơ (refactored to use MedicalReportDAO)";
    }
}
