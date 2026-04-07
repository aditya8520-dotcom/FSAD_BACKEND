package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= REGISTER =================
    @PostMapping("/register")
    public User register(@RequestBody User user) {

        user.setRole("student");

        // ensure email exists
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            user.setEmail(user.getUsername());
        }

        // 🔥 ENCRYPT PASSWORD
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return repo.save(user);
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public User login(@RequestBody User user) {

        System.out.println("Login attempt: " + user.getUsername());

        User existingUser = repo.findByUsername(user.getUsername());

        if (existingUser == null) {
            System.out.println("User NOT FOUND");
            return null;
        }

        // ✅ ADMIN LOGIN (PLAIN PASSWORD)
        if ("admin".equals(existingUser.getRole())) {
            if (existingUser.getPassword().equals(user.getPassword())) {
                System.out.println("ADMIN LOGIN SUCCESS");
                return existingUser;
            } else {
                System.out.println("ADMIN PASSWORD WRONG");
                return null;
            }
        }

        // ✅ STUDENT LOGIN (BCRYPT)
        if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            System.out.println("STUDENT LOGIN SUCCESS");
            return existingUser;
        }

        System.out.println("PASSWORD MISMATCH");
        return null;
    }

    // ================= ALL USERS =================
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return repo.findAll();
    }

    // ================= STUDENTS =================
    @GetMapping("/students")
    public List<User> getStudents() {
        return repo.findAll()
                   .stream()
                   .filter(u -> "student".equals(u.getRole()))
                   .toList();
    }
}