package controller.treatment;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * InputMedicalReportServlet — Đã được refactor.
 *
 * Toàn bộ logic submit phiếu khám đã được chuyển sang:
 *   → SubmitMedicalReportServlet
 *
 * Servlet này chỉ còn nhiệm vụ delegate POST request sang SubmitMedicalReportServlet.
 *
 * @author Refactored — duplicate doPost() logic removed
 */
public class InputMedicalReportServlet extends HttpServlet {

    /**
     * Delegate POST sang SubmitMedicalReportServlet.
     * Giữ nguyên tất cả request parameters để không làm vỡ form hiện tại.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/SubmitMedicalReport").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/view/jsp/doctor/doctor_trongngay.jsp");
    }

    @Override
    public String getServletInfo() {
        return "InputMedicalReportServlet — Delegates to SubmitMedicalReportServlet";
    }
}
