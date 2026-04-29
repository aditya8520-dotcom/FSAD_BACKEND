package com.example.demo.controller;

import com.example.demo.entity.MfaDetails;
import com.example.demo.entity.User;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MfaService mfaService;

    // ✅ REGISTER (dummy)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        user.setRole("student");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ❌ No DB → just return data
        return ResponseEntity.ok(user);
    }

    // ✅ LOGIN (dummy – no DB)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        String username = request.get("username");

        // ❌ No DB → create fake user
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setRole("student");

        Map<String, Object> res = new HashMap<>();
        res.put("status", "MFA_VERIFY");
        res.put("userId", user.getId());

        return ResponseEntity.ok(res);
    }

    // ✅ VERIFY OTP (dummy)
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> req) {

        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("userId", 1);
        res.put("username", "demo");
        res.put("role", "student");

        return ResponseEntity.ok(res);
    }
}