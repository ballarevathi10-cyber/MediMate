package com.medimate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.time.LocalDate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/ReportServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 5 * 1024 * 1024,
    maxRequestSize = 6 * 1024 * 1024
)
public class ReportServlet extends HttpServlet {
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

        String title = request.getParameter("title");
        String category = request.getParameter("category");
        String dateText = request.getParameter("report_date");

        try {
            Part filePart = request.getPart("report_file");

            if (title == null || title.isBlank() ||
                filePart == null || filePart.getSize() == 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Title and report file are required.");
                return;
            }

            String contentType = filePart.getContentType();

            if (contentType == null ||
                !(contentType.equals("application/pdf") ||
                  contentType.equals("image/jpeg") ||
                  contentType.equals("image/png"))) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Only PDF, JPG, and PNG files are allowed.");
                return;
            }

            // Save file name only for now; secure file storage is a
            // separate step before using report uploads in production.
            String fileName = java.nio.file.Paths
                    .get(filePart.getSubmittedFileName())
                    .getFileName().toString();

            Date reportDate = (dateText == null || dateText.isBlank())
                    ? Date.valueOf(LocalDate.now())
                    : Date.valueOf(dateText);

            String sql = "INSERT INTO medical_reports "
                    + "(user_id, title, category, report_date, file_name, file_path) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setString(2, title.trim());
                ps.setString(3, category);
                ps.setDate(4, reportDate);
                ps.setString(5, fileName);
                ps.setString(6, null); // File storage will be added next.

                ps.executeUpdate();
            }

            response.sendRedirect("reports.html?success=1");

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Please enter a valid report date.");
        } catch (Exception e) {
            getServletContext().log("Report upload error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save report details.");
        }
    }
}

