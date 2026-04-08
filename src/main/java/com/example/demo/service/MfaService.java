package com.example.demo.service;
import com.google.zxing.qrcode.QRCodeWriter;

import com.example.demo.entity.MfaDetails;
import com.example.demo.entity.User;
import com.example.demo.repository.MfaDetailsRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class MfaService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Autowired
    private MfaDetailsRepository repo;

    // ✅ CORRECT METHOD (used in controller)
    public MfaDetails findByUser(User user) {
        return repo.findByUser(user);
    }

    // ❌ REMOVE THIS BROKEN METHOD (not needed)
    // public MfaDetails findByUserId(Long userId) { ... }

    // ✅ CREATE MFA (ONLY FIRST TIME)
    public MfaDetails createMfaDetails(User user) {
        GoogleAuthenticatorKey key = gAuth.createCredentials();

        MfaDetails mfa = new MfaDetails();
        mfa.setUser(user);
        mfa.setSecretKey(key.getKey());
        mfa.setMfaEnabled(true);

        return repo.save(mfa);
    }

    // ✅ QR URL
    public String buildOtpAuthUrl(User user, String secret) {
        return String.format(
                "otpauth://totp/FitWell:%s?secret=%s&issuer=FitWell",
                user.getUsername(),
                secret
        );
    }

    // ✅ VERIFY OTP
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    // ✅ GENERATE QR IMAGE
    public String createQrCodeDataUrl(String text) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", stream);

        return "data:image/png;base64," +
                Base64.getEncoder().encodeToString(stream.toByteArray());
    }
}