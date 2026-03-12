package controller.profile;

import dao.StaffDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import model.Staff;
import model.User;

@WebServlet(name = "StaffUpdateServlet", urlPatterns = { "/StaffUpdateServlet" })
public class StaffUpdateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        Staff currentStaff = (Staff) session.getAttribute("staff");

        if (currentUser == null || currentStaff == null) {
            response.sendRedirect(request.getContextPath() + "/view/jsp/auth/login.jsp");
            return;
        }

        String type = request.getParameter("type");
        String success = null;
        String error = null;

        try {
            if ("email".equals(type)) {
                String newEmail = request.getParameter("value");
                if (UserDAO.updateUserAccount(currentUser.getId(), newEmail, null)) {
                    currentUser.setEmail(newEmail);
                    session.setAttribute("user", currentUser);
                    success = "Cập nhật email thành công!";
                } else {
                    error = "Cập nhật email thất bại.";
                }
            } else if ("password".equals(type)) {
                String oldPass = request.getParameter("oldPassword");
                String newPass = request.getParameter("newPassword");
                String confirmPass = request.getParameter("confirmPassword");

                if (!newPass.equals(confirmPass)) {
                    error = "Mật khẩu xác nhận không khớp.";
                } else {
                    String oldPassHash = UserDAO.hashPassword(oldPass);
                    User freshUser = UserDAO.getUserById(currentUser.getId());
                    String dbPass = freshUser.getPasswordHash();

                    // Hỗ trợ cả mật khẩu đã hash và chưa hash (legacy)
                    boolean isOldPassCorrect = (dbPass != null)
                            && (dbPass.equals(oldPass) || dbPass.equalsIgnoreCase(oldPassHash));

                    if (!isOldPassCorrect) {
                        error = "Mật khẩu cũ không đúng.";
                    } else if (UserDAO.updateUserAccount(currentUser.getId(), currentUser.getEmail(), newPass)) {
                        success = "Đổi mật khẩu thành công!";
                    } else {
                        error = "Đổi mật khẩu thất bại.";
                    }
                }
            } else if ("update_staff_info".equals(type)) {
                String fullName = request.getParameter("fullName");
                String phone = request.getParameter("phone");
                String dobStr = request.getParameter("dateOfBirth");
                String gender = request.getParameter("gender");
                String address = request.getParameter("address");

                currentStaff.setFullName(fullName);
                currentStaff.setPhone(phone);
                try {
                    currentStaff.setDateOfBirth(Date.valueOf(dobStr));
                } catch (Exception e) {
                    error = "Ngày sinh không hợp lệ.";
                }
                currentStaff.setGender(gender);
                currentStaff.setAddress(address);

                if (error == null) {
                    if (StaffDAO.updateStaff(currentStaff)) {
                        session.setAttribute("staff", currentStaff);
                        success = "Cập nhật thông tin cá nhân thành công!";
                    } else {
                        error = "Cập nhật thông tin thất bại.";
                    }
                }
            }
        } catch (Exception e) {
            error = "Lỗi: " + e.getMessage();
            e.printStackTrace();
        }

        if (success != null)
            session.setAttribute("success", success);
        if (error != null)
            session.setAttribute("error", error);

        response.sendRedirect(request.getContextPath() + "/StaffProfileServlet");
    }
}
