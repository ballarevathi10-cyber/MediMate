
package com.medimate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Time;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/MedicineServlet")
public class MedicineServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        // Check login session
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // Get form values
        String name = request.getParameter("medicine_name");
        String dosage = request.getParameter("dosage");
        String timeText = request.getParameter("reminder_time");
        String notes = request.getParameter("notes");

        if (name == null || name.isBlank()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Medicine name is required."
            );
            return;
        }

        Time reminderTime = null;

        try {
            // Convert HTML time input to SQL Time
            if (timeText != null && !timeText.isBlank()) {
                reminderTime = Time.valueOf(
                    timeText.length() == 5
                        ? timeText + ":00"
                        : timeText
                );
            }

            String sql = "INSERT INTO medicines "
                       + "(user_id, medicine_name, dosage, reminder_time, notes) "
                       + "VALUES (?, ?, ?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setString(2, name.trim());
                ps.setString(3, dosage);

                if (reminderTime == null) {
                    ps.setNull(4, java.sql.Types.TIME);
                } else {
                    ps.setTime(4, reminderTime);
                }

                ps.setString(5, notes);
                ps.executeUpdate();
            }

            response.sendRedirect("medicines.html?success=1");

        } catch (IllegalArgumentException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Please enter a valid reminder time."
            );

        } catch (Exception e) {
            getServletContext().log("Medicine save error", e);

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to save medicine. Please check the server logs."
            );
        }
    }
}
