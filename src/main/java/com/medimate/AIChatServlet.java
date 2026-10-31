
package com.medimate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AIChatServlet")
public class AIChatServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        String question = request.getParameter("message");

        if (question == null || question.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"error\":\"Please enter a question.\"}");
            return;
        }

        String apiKey = System.getenv("GROQ_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(500);
            response.getWriter().write(
                "{\"error\":\"GROQ_API_KEY is not configured.\"}");
            return;
        }

        HttpURLConnection connection = null;

        try {
            URL url = new URL(
                "https://api.groq.com/openai/v1/chat/completions");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty(
                "Authorization", "Bearer " + apiKey);
            connection.setRequestProperty(
                "Content-Type", "application/json");

            String safeQuestion = question
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

            String body =
                "{\"model\":\"llama-3.3-70b-versatile\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":"
                + "\"You are MediMate, a health information assistant. "
                + "Give general educational information, do not diagnose "
                + "or prescribe medicines, and advise consulting a "
                + "qualified healthcare professional when appropriate.\"},"
                + "{\"role\":\"user\",\"content\":\""
                + safeQuestion + "\"}]}";

            byte[] data = body.getBytes(StandardCharsets.UTF_8);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(data);
            }

            int status = connection.getResponseCode();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream(),
                    StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            String line;

            try (reader) {
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            response.setStatus(status >= 200 && status < 300
                ? 200 : status);
            response.getWriter().write(result.toString());

        } catch (Exception e) {
            getServletContext().log("Groq API error", e);
            response.setStatus(502);
            response.getWriter().write(
                "{\"error\":\"Could not connect to the AI service.\"}");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
