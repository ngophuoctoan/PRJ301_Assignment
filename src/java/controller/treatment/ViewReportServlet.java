package controller.treatment;

import dao.DoctorDAO;
import dao.MedicalReportDAO;
import model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewReportServlet — Hiển thị hồ sơ bệnh án cho Doctor.
 *
 * Đã được refactor:
 *   - DoctorDAO.getMedicalReportByAppointmentId() → MedicalReportDAO.getMedicalReportByAppointmentId()
 *   - DoctorDAO.getMedicalReportById()            → MedicalReportDAO.getMedicalReportById()
 *   - MedicineDAO.getPrescriptionsByReportId()    → MedicalReportDAO.getPrescriptionsByReportId()
 *   - DoctorDAO.getTimeSlotByAppointmentId()      → giữ nguyên (không liên quan MedicalReport)
 *
 * @author Refactored
 */
public class ViewReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String reportIdParam      = request.getParameter("reportId");
            String appointmentIdParam = request.getParameter("appointmentId");

            if (reportIdParam == null && appointmentIdParam == null) {
                response.sendRedirect(request.getContextPath()
                        + "/view/jsp/doctor/doctor_trongngay.jsp?error=missing_params");
                return;
            }

            MedicalReport report = null;
            Integer appointmentId = null;

            // ------------------------------------------------------------------
            // Tìm MedicalReport — ưu tiên reportId, fallback appointmentId
            // ------------------------------------------------------------------
            if (reportIdParam != null) {
                int reportId = Integer.parseInt(reportIdParam);
                report = MedicalReportDAO.getMedicalReportById(reportId);
            }

            if (report == null && appointmentIdParam != null) {
                appointmentId = Integer.parseInt(appointmentIdParam);
                report = MedicalReportDAO.getMedicalReportByAppointmentId(appointmentId);
            }

            if (report == null) {
                String redirectUrl = request.getContextPath()
                        + "/view/jsp/doctor/doctor_no_report_found.jsp";
                if (appointmentId != null) {
                    redirectUrl += "?appointmentId=" + appointmentId;
                } else if (appointmentIdParam != null) {
                    redirectUrl += "?appointmentId=" + appointmentIdParam;
                }
                response.sendRedirect(redirectUrl);
                return;
            }

            // ------------------------------------------------------------------
            // Tải thông tin bổ sung: bệnh nhân, bác sĩ, toa thuốc, time slot
            // ------------------------------------------------------------------
            Patients patient = dao.PatientDAO.getPatientById(report.getPatientId());
            Doctors  doctor  = DoctorDAO.getDoctorById((int) report.getDoctorId());

            // Toa thuốc → map sang PrescriptionDetail để JSP dùng
            List<Prescription> prescriptionList = MedicalReportDAO.getPrescriptionsByReportId(report.getReportId());
            List<PrescriptionDetail> prescriptions = new ArrayList<>();
            if (prescriptionList != null) {
                for (Prescription p : prescriptionList) {
                    prescriptions.add(new PrescriptionDetail(
                            p.getPrescriptionId(),
                            p.getMedicineId(),
                            p.getName() != null ? p.getName() : "",
                            p.getQuantity(),
                            p.getUsage() != null ? p.getUsage() : "",
                            ""
                    ));
                }
            }

            // Time slot (DoctorDAO — không liên quan đến MedicalReport)
            String timeSlot = DoctorDAO.getTimeSlotByAppointmentId(report.getAppointmentId());

            // ------------------------------------------------------------------
            // Gắn attributes và forward tới JSP
            // ------------------------------------------------------------------
            request.setAttribute("report",        report);
            request.setAttribute("patient",       patient);
            request.setAttribute("doctor",        doctor);
            request.setAttribute("prescriptions", prescriptions);
            request.setAttribute("appointmentId", report.getAppointmentId());
            request.setAttribute("timeSlot",      timeSlot);

            request.getRequestDispatcher("/view/jsp/doctor/doctor_viewMedicalReport.jsp")
                   .forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/view/jsp/doctor/error_page.jsp?error=invalid_id");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()
                    + "/view/jsp/doctor/error_page.jsp?error=system_error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "ViewReportServlet — Xem hồ sơ bệnh án (refactored to use MedicalReportDAO)";
    }
}