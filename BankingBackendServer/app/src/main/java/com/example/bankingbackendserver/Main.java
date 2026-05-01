package com.example.bankingbackendserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;

import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class Main {
    public static final String ACCOUNT_SID = "Account_sid";
    public static final String AUTH_TOKEN = "Auth_token";
    public static final Map<String, String> otpStorage = new HashMap<>();

    public static String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Testing OTP generation and encryption...");

        // Measure OTP generation time
        long startGen = System.nanoTime();
        String otp = generateOtp();
        long endGen = System.nanoTime();
        long generationTime = (endGen - startGen) / 1_000_000;

        System.out.println("Generated OTP: " + otp);
        System.out.println("OTP Generation Time: " + generationTime + " ms");

        // Then proceed with encryption timing as before...
        long startEnc = System.nanoTime();
        String encryptedOtp = AESEncryptionUtil.encrypt(otp);
        long endEnc = System.nanoTime();
        long encryptionTime = (endEnc - startEnc) / 1_000_000;

        System.out.println("Encrypted OTP: " + encryptedOtp);
        System.out.println("Encryption Time: " + encryptionTime + " ms");

        long startDec = System.nanoTime();
        String decryptedOtp = AESEncryptionUtil.decrypt(encryptedOtp);
        long endDec = System.nanoTime();
        long decryptionTime = (endDec - startDec) / 1_000_000;

        System.out.println("Decrypted OTP: " + decryptedOtp);
        System.out.println("Decryption Time: " + decryptionTime + " ms");

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8089), 0);
        server.createContext("/signup", new SignupHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/verifyOtp", new VerifyOtpHandler());
        server.createContext("/getLatestOtp", new GetLatestOtpHandler());
        server.createContext("/loginAppA", new AppALoginHandler());
        server.createContext("/verifyAppASpecialCode", new VerifyAppASpecialCodeHandler());
        server.createContext("/test", exchange -> {
            String response = "Backend is alive!";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8089");

        startOtpCleanupTask();

    }

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
/*
    // This is RSA Encryption Technique but i use the same class name so that not to modify other classes
    public class AESEncryptionUtil {

        private static final String RSA = "RSA";
        private static final int KEY_SIZE = 2048;
        private static final String PUBLIC_KEY_FILE = "public.key";
        private static final String PRIVATE_KEY_FILE = "private.key";

        private static PublicKey publicKey;
        private static PrivateKey privateKey;

        static {
            try {
                File pubKeyFile = new File(PUBLIC_KEY_FILE);
                File privKeyFile = new File(PRIVATE_KEY_FILE);

                if (pubKeyFile.exists() && privKeyFile.exists()) {
                    // Load public key bytes and generate key
                    byte[] pubKeyBytes = java.nio.file.Files.readAllBytes(pubKeyFile.toPath());
                    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
                    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
                    publicKey = keyFactory.generatePublic(pubKeySpec);

                    // Load private key bytes and generate key
                    byte[] privKeyBytes = java.nio.file.Files.readAllBytes(privKeyFile.toPath());
                    PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
                    privateKey = keyFactory.generatePrivate(privKeySpec);

                } else {
                    // Generate key pair
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA);
                    keyGen.initialize(KEY_SIZE);
                    KeyPair keyPair = keyGen.generateKeyPair();
                    publicKey = keyPair.getPublic();
                    privateKey = keyPair.getPrivate();

                    // Save keys as bytes (not serialized objects)
                    java.nio.file.Files.write(pubKeyFile.toPath(), publicKey.getEncoded());
                    java.nio.file.Files.write(privKeyFile.toPath(), privateKey.getEncoded());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static String encrypt(String plainText) {
            try {
                Cipher cipher = Cipher.getInstance(RSA);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static String decrypt(String encryptedText) {
            try {
                Cipher cipher = Cipher.getInstance(RSA);
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
                return new String(decryptedBytes, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }


*/
//This is AES Encryption
 /*   public class AESEncryptionUtil {

        private static final String AES = "AES";
        private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
        private static final int GCM_TAG_LENGTH = 128; // bits
        private static final int IV_LENGTH = 12; // bytes

        // Ideally, store this key securely, e.g., environment variable or config file.
        private static final byte[] keyBytes = new byte[] {
                // 16 bytes (128-bit) example key -
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
                0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46
        };
        private static final SecretKey secretKey = new SecretKeySpec(keyBytes, AES);

        public static String encrypt(String plainText) {
            try {
                Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

                byte[] iv = new byte[IV_LENGTH];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(iv);

                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

                byte[] cipherText = cipher.doFinal(plainText.getBytes());

                // Prepend IV to cipher text, then Base64 encode
                byte[] cipherTextWithIv = new byte[IV_LENGTH + cipherText.length];
                System.arraycopy(iv, 0, cipherTextWithIv, 0, IV_LENGTH);
                System.arraycopy(cipherText, 0, cipherTextWithIv, IV_LENGTH, cipherText.length);

                return Base64.getEncoder().encodeToString(cipherTextWithIv);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static String decrypt(String encryptedText) {
            try {
                byte[] cipherTextWithIv = Base64.getDecoder().decode(encryptedText);

                byte[] iv = new byte[IV_LENGTH];
                System.arraycopy(cipherTextWithIv, 0, iv, 0, IV_LENGTH);

                int cipherTextLength = cipherTextWithIv.length - IV_LENGTH;
                byte[] cipherText = new byte[cipherTextLength];
                System.arraycopy(cipherTextWithIv, IV_LENGTH, cipherText, 0, cipherTextLength);

                Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

                byte[] plainTextBytes = cipher.doFinal(cipherText);

                return new String(plainTextBytes);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }*/

    // This is Blowfish Encryption
    public class AESEncryptionUtil {

        private static final String ALGORITHM = "Blowfish";
        private static final String SECRET_KEY = "MyBlowfishKey"; // should be 4–56 bytes

        // Generate SecretKeySpec from the static key
        private static SecretKeySpec getKey() {
            return new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        }

        public static String encrypt(String plainText) {
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, getKey());
                byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
                return Base64.getEncoder().encodeToString(encrypted);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static String decrypt(String encryptedText) {
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, getKey());
                byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
                byte[] decrypted = cipher.doFinal(decodedBytes);
                return new String(decrypted, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void startOtpCleanupTask() {
        Timer timer = new Timer(true); // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                     Statement stmt = conn.createStatement()) {

                    int deleted = stmt.executeUpdate("DELETE FROM otps WHERE created_at < NOW() - INTERVAL 3 MINUTE");
                    if (deleted > 0) {
                        System.out.println("🗑️ Cleaned up " + deleted + " expired OTP(s)");
                    }

                } catch (SQLException e) {
                    System.err.println("❌ Error cleaning expired OTPs:");
                    e.printStackTrace();
                }
            }
        }, 0, 60000); // Run every 60 seconds
    }
    static class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) body.append(line);

            String[] pairs = body.toString().split("&");
            String fullName = null, phoneNumber = null, username = null, password = null;

            for (String pair : pairs) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], "UTF-8");
                    String value = URLDecoder.decode(parts[1], "UTF-8");
                    switch (key) {
                        case "full_name": fullName = value; break;
                        case "phone_number": phoneNumber = value; break;
                        case "username": username = value; break;
                        case "password": password = value; break;
                    }
                }
            }

            String response;
            if (fullName == null || phoneNumber == null || username == null || password == null) {
                response = "Missing fields";
                exchange.sendResponseHeaders(400, response.length());
            } else if (userExists(username)) {
                response = "User already exists";
                exchange.sendResponseHeaders(409, response.length());
            } else {
                String generatedSpecialCode = UUID.randomUUID().toString();
                if (createUser(fullName, phoneNumber, username, password, generatedSpecialCode)) {
                    response = "Signup successful. Your special code: " + generatedSpecialCode;
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response = "Signup failed";
                    exchange.sendResponseHeaders(500, response.length());
                }
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean userExists(String username) {
            String sql = "SELECT id FROM users WHERE username = ?";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return true;
            }
        }

        private boolean createUser(String fullName, String phoneNumber, String username, String password, String specialCode) {
            String hashedPassword = Main.hashPassword(password);
            String sql = "INSERT INTO users (full_name, phone_number, username, password, special_code) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fullName);
                stmt.setString(2, phoneNumber);
                stmt.setString(3, username);
                stmt.setString(4, hashedPassword);
                stmt.setString(5, specialCode);
                int rowsInserted = stmt.executeUpdate();
                return rowsInserted > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) body.append(line);

            String[] pairs = body.toString().split("&");
            String username = null, password = null;

            for (String pair : pairs) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], "UTF-8");
                    String value = URLDecoder.decode(parts[1], "UTF-8");
                    if (key.equals("username")) username = value;
                    else if (key.equals("password")) password = hashPassword(value);
                }
            }

            String response;
            if (username != null && password != null && validateLogin(username, password)) {
                String otp = generateOtp();
                String encryptedOtp = AESEncryptionUtil.encrypt(otp);  // Encrypt the OTP
                String phoneNumber = getUserPhoneNumber(username);
                if (phoneNumber != null) {
                    otpStorage.put(username, encryptedOtp); // store encrypted OTP in memory
                    saveOtpToDatabase(username, encryptedOtp); // save encrypted OTP to DB
                    //sendOtpWithTwilio(phoneNumber, otp); // send plain OTP via SMS
                    response = "Login successful. OTP sent.";
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response = "Phone number not found.";
                    exchange.sendResponseHeaders(500, response.length());
                }
            } else {
                response = "Login failed.";
                exchange.sendResponseHeaders(401, response.length());
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean validateLogin(String username, String hashedPassword) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                return stmt.executeQuery().next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private String getUserPhoneNumber(String username) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement("SELECT phone_number FROM users WHERE username = ?")) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getString("phone_number");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void sendOtpWithTwilio(String phoneNumber, String otp) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message.creator(new PhoneNumber(phoneNumber),
                    new PhoneNumber("+16203198649"),
                    "Your OTP is: " + otp).create();
        }

        private String generateOtp() {
            return String.valueOf(new Random().nextInt(900000) + 100000);

        }

        private void saveOtpToDatabase(String username, String encryptedOtp) {
            String sql = "INSERT INTO otps (username, otp) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, encryptedOtp);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static class VerifyOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) body.append(line);

            String[] pairs = body.toString().split("&");
            String username = null, otpInput = null;

            for (String pair : pairs) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], "UTF-8");
                    String value = URLDecoder.decode(parts[1], "UTF-8");
                    if (key.equals("username")) username = value;
                    else if (key.equals("otp")) otpInput = value;
                }
            }

            String response;
            if (username == null || otpInput == null) {
                response = "Missing username or OTP.";
                exchange.sendResponseHeaders(400, response.length());
            } else {
                // Get the latest encrypted OTP
                String encryptedOtp = getLatestEncryptedOtp(username);
                if (encryptedOtp == null) {
                    response = "No OTP found for user.";
                    exchange.sendResponseHeaders(404, response.length());
                } else {
                    try {
                        String decryptedOtp = AESEncryptionUtil.decrypt(encryptedOtp);
                        if (otpInput.equals(decryptedOtp)) {
                            deleteLatestOtp(username); // Only delete that OTP
                            response = "OTP verification successful.";
                            exchange.sendResponseHeaders(200, response.length());
                        } else {
                            response = "OTP verification failed.";
                            exchange.sendResponseHeaders(401, response.length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response = "Error decrypting OTP.";
                        exchange.sendResponseHeaders(500, response.length());
                    }
                }
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String getLatestEncryptedOtp(String username) {
            String sql = "SELECT otp FROM otps WHERE username = ? ORDER BY created_at DESC LIMIT 1";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("otp");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void deleteLatestOtp(String username) {
            String sql = "DELETE FROM otps WHERE id = (" +
                    "SELECT id FROM otps WHERE username = ? ORDER BY created_at DESC LIMIT 1)";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    static class GetLatestOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            String username = null;

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("username")) {
                        username = URLDecoder.decode(pair[1], "UTF-8");
                        break;
                    }
                }
            }

            String response;
            if (username != null) {
                String otp = getLatestOtpFromDatabase(username);
                if (otp != null) {
                    response = otp;
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response = "No OTP found";
                    exchange.sendResponseHeaders(404, response.length());
                }
            } else {
                response = "Username parameter is missing";
                exchange.sendResponseHeaders(400, response.length());
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String getLatestOtpFromDatabase(String username) {
            String sql = "SELECT otp FROM otps WHERE username = ? ORDER BY created_at DESC LIMIT 1";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String encryptedOtp = rs.getString("otp");
                    try {
                        return AESEncryptionUtil.decrypt(encryptedOtp); // ✅ Decrypt the OTP before returning
                    } catch (Exception e) {
                        System.err.println("Error decrypting OTP: " + e.getMessage());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    static class AppALoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            String[] pairs = body.toString().split("&");
            String username = null;
            String password = null;

            for (String pair : pairs) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], "UTF-8");
                    String value = URLDecoder.decode(parts[1], "UTF-8");
                    switch (key) {
                        case "username":
                            username = value;
                            break;
                        case "password":
                            password = Main.hashPassword(value); // Hash before checking
                            break;
                    }
                }
            }

            String response;
            if (username == null || password == null) {
                response = "Missing credentials";
                exchange.sendResponseHeaders(400, response.length());
            } else if (validateLogin(username, password)) {
                response = "LoginAppA success"; // App A should move to Special Code screen now
                exchange.sendResponseHeaders(200, response.length());
            } else {
                response = "LoginAppA failed";
                exchange.sendResponseHeaders(401, response.length());
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean validateLogin(String username, String hashedPassword) {
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                ResultSet rs = stmt.executeQuery();
                return rs.next(); // returns true if user found
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    static class VerifyAppASpecialCodeHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // Read and parse request body
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String body = reader.lines().collect(Collectors.joining());
            Map<String, String> params = parseFormData(body);
            String specialCode = params.get("specialCode");

            String response;
            if (specialCode != null && !specialCode.isEmpty()) {
                String username = getUsernameBySpecialCode(specialCode);
                if (username != null) {
                    response = "VALID:" + username;
                } else {
                    response = "INVALID";
                }
            } else {
                response = "INVALID";
            }

            // Send response
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private Map<String, String> parseFormData(String formData) {
            Map<String, String> params = new HashMap<>();
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
            return params;
        }

        private String getUsernameBySpecialCode(String specialCode) {

            String query = "SELECT username FROM users WHERE special_code = ?";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8088/bankingappdb", "root", "Maman78pere@7824");
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, specialCode);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getString("username");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
