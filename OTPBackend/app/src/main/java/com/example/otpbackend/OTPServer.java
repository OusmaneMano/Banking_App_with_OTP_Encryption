package com.example.otpbackend;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;

public class OTPServer {

    // Replace with your Twilio credentials
    public static final String ACCOUNT_SID = "A_sid";
    public static final String AUTH_TOKEN = "A_Token";
    public static final String TWILIO_PHONE_NUMBER = "Phone";

    public static void main(String[] args) throws IOException {
        // Initialize Twilio
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // Create an HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/send-otp", new SendOtpHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server started at http://localhost:8080/");
    }

    static class SendOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes());

                // Expect JSON format like: {"phoneNumber": "+905338589602"}
                String phoneNumber = extractPhoneNumber(requestBody);

                if (phoneNumber == null) {
                    String response = "Invalid request. 'phoneNumber' is required.";
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }

                String otp = generateOtp();

                try {
                    // Send OTP via Twilio
                    Message.creator(
                            new PhoneNumber(phoneNumber),
                            new PhoneNumber(TWILIO_PHONE_NUMBER),
                            "Your OTP code is: " + otp
                    ).create();

                    String response = "OTP sent successfully to " + phoneNumber;
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    String response = "Failed to send OTP: " + e.getMessage();
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }

        private String generateOtp() {
            Random random = new Random();
            int otp = 100000 + random.nextInt(900000); // 6-digit OTP
            return String.valueOf(otp);
        }

        // A very basic JSON parser to extract the phoneNumber value
        private String extractPhoneNumber(String json) {
            String key = "\"phoneNumber\"";
            int index = json.indexOf(key);
            if (index == -1) return null;

            int start = json.indexOf("\"", index + key.length());
            int end = json.indexOf("\"", start + 1);
            if (start == -1 || end == -1) return null;

            return json.substring(start + 1, end);
        }
    }
}
