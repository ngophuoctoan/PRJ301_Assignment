/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.profile;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Patients;
import model.User;

/**
 *
 * @author Home
 */

public class UserAccountServlet extends HttpServlet {

    
     @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/view/jsp/auth/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        Patients patient = (Patients) session.getAttribute("patient");

        if (user == null && patient == null) {
            request.setAttribute("error", "Không tìm thấy thông tin người dùng.");
        }

        // Nếu cần load thêm dữ liệu từ DB, bạn có thể làm ở đây

        // Forward về JSP
        request.getRequestDispatcher("/view/jsp/patient/user_taikhoan.jsp").forward(request, response);
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    }

    

}
