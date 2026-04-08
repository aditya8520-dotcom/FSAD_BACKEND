package com.example.demo.controller;

import com.example.demo.entity.MfaDetails;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MfaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MfaService mfaService;

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        user.setRole("student");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(repo.save(user));
    }

    // ✅ LOGIN WITH MFA
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        String username = request.get("username");
        String password = request.get("password");

        User user = repo.findByUsername(username);
        if (user == null) user = repo.findByEmail(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        boolean valid = passwordEncoder.matches(password, user.getPassword())
                || password.equals(user.getPassword());

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        MfaDetails mfa = mfaService.findByUser(user);

        Map<String, Object> res = new HashMap<>();

        // 🔥 FIRST TIME → GENERATE QR
        if (mfa == null) {
            mfa = mfaService.createMfaDetails(user);

            try {
                String url = mfaService.buildOtpAuthUrl(user, mfa.getSecretKey());
                String qr = mfaService.createQrCodeDataUrl(url);

                res.put("status", "MFA_SETUP");
                res.put("qr", qr);
                res.put("userId", user.getId());

                return ResponseEntity.ok(res);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("QR generation failed");
            }
        }

        // 🔥 NEXT LOGIN → ONLY OTP (NO QR)
        res.put("status", "MFA_VERIFY");
        res.put("userId", user.getId());
        return ResponseEntity.ok(res);
    }

    // ✅ VERIFY OTP
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> req) {

        Long userId = Long.parseLong(req.get("userId"));
        int code = Integer.parseInt(req.get("otp"));

        User user = repo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        MfaDetails mfa = mfaService.findByUser(user);
        if (mfa == null) return ResponseEntity.badRequest().body("MFA not setup");

        boolean ok = mfaService.verifyCode(mfa.getSecretKey(), code);

        if (!ok) {
            return ResponseEntity.status(401).body("Invalid OTP");
        }

        // ✅ RETURN FULL LOGIN SUCCESS DATA
        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("userId", user.getId());
        res.put("username", user.getUsername());
        res.put("role", user.getRole());

        return ResponseEntity.ok(res);
    }
}