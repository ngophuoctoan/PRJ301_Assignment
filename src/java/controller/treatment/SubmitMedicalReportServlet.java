package controller.treatment;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.MedicalReportDAO;
import dao.MedicineDAO;
import dao.PatientDAO;
import model.Doctors;
import model.Patients;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * SubmitMedicalReportServlet — Servlet DUY NHẤT xử lý submit phiếu khám.
 *
 * Thay thế logic trùng lặp trước đây trong:
 *   - MedicalReportServlet.doPost()
 *   - InputMedicalReportServlet.doPost()
 *
 * Luồng xử lý:
 *   doctor_phieukham.jsp (POST)
 *   → SubmitMedicalReportServlet
 *   → MedicalReportDAO.insertMedicalReport()
 *   → [LOOP] MedicineDAO.hasEnoughStock / MedicalReportDAO.insertPrescription / MedicineDAO.reduceMedicineStock
 *   → AppointmentDAO.updateAppointmentStatusStatic("COMPLETED")
 *   → redirect /view/jsp/doctor/success.jsp
 *
 * @author Refactored
 */
public class SubmitMedicalReportServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            // ------------------------------------------------------------------
            // BƯỚC 1: Đọc và validate các request parameters
            // ------------------------------------------------------------------
            String appointmentIdStr = request.getParameter("appointment_id");
            String doctorIdStr      = request.getParameter("doctor_id");
            String patientIdStr     = request.getParameter("patient_id");
            String diagnosis        = request.getParameter("diagnosis");
            String treatmentPlan    = request.getParameter("treatment_plan");
            String note             = request.getParameter("note");
            String sign             = request.getParameter("sign");
            // Checkbox tái khám lần 2: form gửi "1" khi được tick
            boolean isReexamLan2 = "1".equals(request.getParameter("reexam_lan_2"));

            if (isNullOrBlank(appointmentIdStr)) {
                sendError(response, "Lỗi: Thiếu ID cuộc hẹn (appointment_id).");
                return;
            }
            if (isNullOrBlank(doctorIdStr)) {
                sendError(response, "Lỗi: Thiếu ID bác sĩ (doctor_id).");
                return;
            }
            if (isNullOrBlank(patientIdStr)) {
                sendError(response, "Lỗi: Thiếu ID bệnh nhân (patient_id).");
                return;
            }
            if (isNullOrBlank(diagnosis)) {
                sendError(response, "Lỗi: Thiếu chẩn đoán (diagnosis).");
                return;
            }

            int  appointmentId = Integer.parseInt(appointmentIdStr.trim());
            long doctorId      = Long.parseLong(doctorIdStr.trim());
            int  patientId     = Integer.parseInt(patientIdStr.trim());

            // ------------------------------------------------------------------
            // BƯỚC 2: Kiểm tra Patient và Doctor tồn tại trong DB
            // ------------------------------------------------------------------
            Patients patient = PatientDAO.getPatientById(patientId);
            if (patient == null) {
                sendError(response, "Lỗi: Không tìm thấy bệnh nhân với ID: " + patientId);
                return;
            }

            Doctors doctor = DoctorDAO.getDoctorById((int) doctorId);
            if (doctor == null) {
                sendError(response, "Lỗi: Không tìm thấy bác sĩ với ID: " + doctorId);
                return;
            }

            // ------------------------------------------------------------------
            // BƯỚC 3: Tạo MedicalReport → lấy reportId
            // ------------------------------------------------------------------
            int reportId = MedicalReportDAO.insertMedicalReport(
                    appointmentId, doctorId, patientId,
                    diagnosis,
                    treatmentPlan != null ? treatmentPlan.trim() : "",
                    note != null ? note.trim() : "",
                    sign != null ? sign.trim() : "",
                    isReexamLan2
            );

            if (reportId == -1) {
                sendError(response, "Lỗi: Không thể tạo hồ sơ bệnh án. Vui lòng thử lại.");
                return;
            }

            // ------------------------------------------------------------------
            // BƯỚC 4: Xử lý đơn thuốc (nếu có)
            // ------------------------------------------------------------------
            String[] medicineIds = request.getParameterValues("medicine_id");
            String[] quantities  = request.getParameterValues("quantity");
            String[] usages      = request.getParameterValues("usage");

            if (medicineIds != null && medicineIds.length > 0) {
                for (int i = 0; i < medicineIds.length; i++) {
                    // Bỏ qua dòng trống
                    if (isNullOrBlank(medicineIds[i]) || isNullOrBlank(quantities[i])) {
                        continue;
                    }

                    int    medId = Integer.parseInt(medicineIds[i].trim());
                    int    qty   = Integer.parseInt(quantities[i].trim());
                    String usage = (usages != null && i < usages.length && usages[i] != null)
                            ? usages[i].trim() : "";

                    // Kiểm tra tồn kho đủ không
                    if (!MedicineDAO.hasEnoughStock(medId, qty)) {
                        sendError(response,
                                "Lỗi: Không đủ thuốc trong kho cho thuốc ID: " + medId
                                + " (cần " + qty + " đơn vị).");
                        return;
                    }

                    // Lưu toa thuốc
                    MedicalReportDAO.insertPrescription(reportId, medId, qty, usage);

                    // Trừ tồn kho
                    MedicineDAO.reduceMedicineStock(medId, qty);
                }
            }

            // ------------------------------------------------------------------
            // BƯỚC 5: Cập nhật trạng thái lịch hẹn → COMPLETED
            // ------------------------------------------------------------------
            AppointmentDAO.updateAppointmentStatusStatic(appointmentId, "COMPLETED");

            // ------------------------------------------------------------------
            // BƯỚC 6: Redirect về trang thành công
            // ------------------------------------------------------------------
            response.sendRedirect(request.getContextPath() + "/view/jsp/doctor/success.jsp");

        } catch (NumberFormatException e) {
            sendError(response, "Lỗi: Dữ liệu số không hợp lệ — " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    /** GET — hiển thị lại form (không dùng trực tiếp — forward từ CreateMedicalReportServlet). */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/view/jsp/doctor/doctor_trongngay.jsp");
    }

    @Override
    public String getServletInfo() {
        return "SubmitMedicalReportServlet — Servlet duy nhất xử lý submit phiếu khám";
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().println(message);
    }
}
