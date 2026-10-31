
package com.medimate;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");

        PrintWriter out = response.getWriter();

        if (fullName == null || email == null ||
            password == null || confirmPassword == null ||
            fullName.isBlank() || email.isBlank() ||
            password.isBlank() || confirmPassword.isBlank()) {

            out.println("<h3>Please fill all required fields.</h3>");
            return;
        }

        if (!password.equals(confirmPassword)) {
            out.println("<h3>Passwords do not match!</h3>");
            return;
        }

        String sql = "INSERT INTO users "
                   + "(full_name, email, password_hash) "
                   + "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim());
            ps.setString(3, password);

            int result = ps.executeUpdate();

            if (result > 0) {
                response.sendRedirect("index.html?registered=success");
            } else {
                out.println(
                    "<h3>Registration failed. Please try again.</h3>"
                );
            }

        } catch (SQLException e) {

            if ("23000".equals(e.getSQLState())) {
                out.println(
                    "<h3>This email may already be registered.</h3>"
                );
            } else {
                getServletContext().log("Registration error", e);
                out.println(
                    "<h3>Registration failed due to a server error.</h3>"
                );
            }
        }
    }
}
