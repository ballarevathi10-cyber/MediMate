
package com.medimate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null
                || email.isBlank() || password.isBlank()) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Please enter email and password."
            );
            return;
        }

        String sql = "SELECT id, full_name, password_hash "
                   + "FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                    );
                    response.getWriter().println(
                        "<h3>Account not found. Please register first.</h3>"
                    );
                    return;
                }

                String storedPassword = rs.getString("password_hash");

                if (storedPassword == null
                        || !password.equals(storedPassword)) {

                    response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                    );
                    response.getWriter().println(
                        "<h3>Incorrect password. Please try again.</h3>"
                    );
                    return;
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("userId", rs.getInt("id"));
                session.setAttribute(
                    "userName", rs.getString("full_name")
                );

                response.sendRedirect("dashboard.html");
            }

        } catch (Exception e) {
            getServletContext().log("Login error", e);

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Login failed. Please check the server logs."
            );
        }
    }
}
