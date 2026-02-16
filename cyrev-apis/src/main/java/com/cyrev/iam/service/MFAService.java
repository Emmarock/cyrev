package com.cyrev.iam.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MFAService {
    private final SecretGenerator secretGenerator;
    private final CodeVerifier verifier;
    private final QrGenerator qrGenerator;

    // Generate a secret key for a user
    public String generateSecretKey() {
        return secretGenerator.generate();
    }

    // Generate QR code image file for Google Authenticator
    public byte [] generateQRCode(String username, String issuer, String secret) throws IOException, QrGenerationException {
        String qrFile = "QRCode_" + username + ".png";
        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        byte[] qrImage = qrGenerator.generate(data);

        try (FileOutputStream fos = new FileOutputStream(qrFile)) {
            fos.write(qrImage);
        }
        return qrImage;
    }

    // Verify TOTP code
    public boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
