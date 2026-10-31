package com.medimate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/WaterServlet")
public class WaterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Please log in first.");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String amountText = request.getParameter("intake_ml");

        try {
            int amount = Integer.parseInt(amountText);

            if (amount <= 0 || amount > 5000) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Enter a valid water amount.");
                return;
            }

            String sql = "INSERT INTO water_intake "
                    + "(user_id, intake_ml, intake_date) VALUES (?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, amount);
                ps.setDate(3, new Date(System.currentTimeMillis()));

                ps.executeUpdate();
            }

            response.sendRedirect("water.html?success=1");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Water amount must be a number.");
        } catch (Exception e) {
            getServletContext().log("Water tracking error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save water intake.");
        }
    }
}

