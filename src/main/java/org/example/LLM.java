package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LLM {

    private static String model = "gpt-oss:20b";


    public static String generate(String prompt) {
        try {
            URL url = new URL("http://localhost:11434/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // JSON-тело запроса
            String jsonInput = String.format(
                    "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}",
                    model, prompt.replace("\"", "\\\"")
            );

            // Отправляем запрос
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Читаем ответ
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Парсим JSON и возвращаем "response"
            String jsonResponse = response.toString();
            String key = "\"response\":\"";
            int start = jsonResponse.indexOf(key);
            if (start == -1) return "Ошибка: нет ответа от модели";

            start += key.length();
            int end = jsonResponse.indexOf("\"", start);
            return jsonResponse.substring(start, end);

        } catch (Exception e) {
            return "Ошибка AI: " + e.getMessage();
        }
    }
}