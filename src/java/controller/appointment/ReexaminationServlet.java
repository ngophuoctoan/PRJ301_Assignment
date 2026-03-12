package controller.appointment;

import dao.AppointmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import model.Appointment;
import model.User;

/**
 * Servlet trang Tái khám (doctor).
 * GET  → forward tới doctor_taikham.jsp
 * POST → tạo appointment tái khám mới từ form modal
 */
public class ReexaminationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null || !"DOCTOR".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/view/jsp/auth/login.jsp");
            return;
        }
        request.getRequestDispatcher("/view/jsp/doctor/doctor_taikham.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null || !"DOCTOR".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/view/jsp/auth/login.jsp");
            return;
        }

        String redirectBase = request.getContextPath() + "/ReexaminationServlet";

        try {
            // Lấy dữ liệu từ form
            String appointmentIdStr = request.getParameter("appointmentId");
            String reexamDateStr    = request.getParameter("reexamDate");
            String note             = request.getParameter("note");

            if (appointmentIdStr == null || reexamDateStr == null || reexamDateStr.isEmpty()) {
                response.sendRedirect(redirectBase + "?result=error&msg=missing_params");
                return;
            }

            int originalAppointmentId = Integer.parseInt(appointmentIdStr.trim());
            LocalDate reexamDate      = LocalDate.parse(reexamDateStr.trim());

            // Không cho đặt tái khám trong quá khứ
            if (reexamDate.isBefore(LocalDate.now())) {
                response.sendRedirect(redirectBase + "?result=error&msg=date_past");
                return;
            }

            // Lấy thông tin appointment gốc để có patient_id, doctor_id, service_id
            Appointment original = AppointmentDAO.getAppointmentById(originalAppointmentId);
            if (original == null) {
                response.sendRedirect(redirectBase + "?result=error&msg=appt_not_found");
                return;
            }

            // Tạo appointment tái khám mới
            Appointment reexam = new Appointment();
            reexam.setPatientId(original.getPatientId());
            reexam.setDoctorId(original.getDoctorId());
            reexam.setServiceId(original.getServiceId());
            // slot_id = 1 (slot mặc định - bệnh nhân có thể đổi sau)
            reexam.setSlotId(1);
            reexam.setWorkDate(reexamDate);
            reexam.setStatus(AppointmentDAO.STATUS_BOOKED);

            // Reason = ghi chú tái khám
            String reason = "Tái khám";
            if (note != null && !note.trim().isEmpty()) {
                reason += ": " + note.trim();
            }
            reexam.setReason(reason);

            int newId = AppointmentDAO.createAppointment(reexam);
            if (newId > 0) {
                response.sendRedirect(redirectBase + "?result=success&newAppointmentId=" + newId);
            } else {
                response.sendRedirect(redirectBase + "?result=error&msg=db_error");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(redirectBase + "?result=error&msg=invalid_id");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(redirectBase + "?result=error&msg=server_error");
        }
    }
}
