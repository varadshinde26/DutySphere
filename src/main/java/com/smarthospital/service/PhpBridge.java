package com.smarthospital.service;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class PhpBridge {

    private PhpBridge() {
    }

    public static void sendDutyMail(String email, String name, String duty) {
        sendNotificationMail(
                email,
                name,
                "Duty assignment notification",
                "Duty assigned: " + duty
        );
    }

    public static void sendNotificationMail(
            String email, String name, String subject, String message) {

        if (email == null || email.isBlank() || message == null || message.isBlank()) {
            return;
        }

        String url = System.getenv().getOrDefault(
                "HOSPITAL_PHP_URL",
                "http://localhost/smart-hospital-php/notify.php"
        );

        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();

            c.setRequestMethod("POST");
            c.setConnectTimeout(5000);
            c.setReadTimeout(10000);
            c.setDoOutput(true);
            c.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
            );

            String body = "email=" + URLEncoder.encode(email, "UTF-8")
                    + "&name=" + URLEncoder.encode(name == null ? "User" : name, "UTF-8")
                    + "&subject=" + URLEncoder.encode(subject, "UTF-8")
                    + "&message=" + URLEncoder.encode(message, "UTF-8");

            try (OutputStream o = c.getOutputStream()) {
                o.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = c.getResponseCode();
            InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();

            if (in != null) {
                try (InputStream stream = in) {
                    stream.transferTo(OutputStream.nullOutputStream());
                }
            }

            c.disconnect();

        } catch (Exception ignored) {
            /* In-app notifications remain available if email service is offline. */
        }
    }
}