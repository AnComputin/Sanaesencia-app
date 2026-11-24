
package com.sana_esencia.usuarios_service.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public class Pbkdf2PasswordEncoderCompat implements PasswordEncoder {

    private static final int ITERATIONS = 600000;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;

    private static final int MIN_ITERATIONS = 1000;
    private static final int MAX_ITERATIONS = 1000000;

    private static final String PREFIX = "pbkdf2:sha256:";

    private final SecureRandom random = new SecureRandom();

    @Override
    public String encode(CharSequence rawPassword) {

        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        byte[] hash = derive(
                rawPassword.toString(),
                salt,
                ITERATIONS,
                HASH_LENGTH
        );

        return PREFIX + ITERATIONS + ":"
                + HexFormat.of().formatHex(salt) + ":"
                + HexFormat.of().formatHex(hash);
    }

    @Override
    public boolean matches(
            CharSequence rawPassword,
            String encodedPassword
    ) {

        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        try {

            if (encodedPassword.startsWith(PREFIX)) {
                return matchesPbkdf2(rawPassword.toString(), encodedPassword);
            }

            if (esSha256Legacy(encodedPassword)) {
                return matchesSha256Legacy(
                        rawPassword.toString(),
                        encodedPassword
                );
            }

            // Compatibilidad transitoria con contraseñas históricas
            // en texto plano.
            return MessageDigest.isEqual(
                    rawPassword.toString().getBytes(StandardCharsets.UTF_8),
                    encodedPassword.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception e) {
            return false;
        }
    }

    public boolean esHashActual(String encodedPassword) {
        return encodedPassword != null
                && encodedPassword.startsWith(PREFIX);
    }

    private boolean matchesPbkdf2(String password, String stored) {

        String[] parts = stored.split(":", -1);

        if (parts.length != 5
                || !parts[0].equals("pbkdf2")
                || !parts[1].equals("sha256")) {
            return false;
        }

        int iterations = Integer.parseInt(parts[2]);

        if (iterations < MIN_ITERATIONS
                || iterations > MAX_ITERATIONS) {
            return false;
        }

        byte[] salt = HexFormat.of().parseHex(parts[3]);
        byte[] expected = HexFormat.of().parseHex(parts[4]);

        if (salt.length != SALT_LENGTH
                || expected.length != HASH_LENGTH) {
            return false;
        }

        byte[] calculated = derive(
                password,
                salt,
                iterations,
                expected.length
        );

        return MessageDigest.isEqual(calculated, expected);
    }

    private boolean esSha256Legacy(String stored) {

        String[] parts = stored.split(":", -1);

        return parts.length == 2
                && parts[0].matches("[0-9a-fA-F]{32}")
                && parts[1].matches("[0-9a-fA-F]{64}");
    }

    private boolean matchesSha256Legacy(String password, String stored)
            throws Exception {

        String[] parts = stored.split(":", -1);

        byte[] salt = HexFormat.of().parseHex(parts[0]);
        byte[] expected = HexFormat.of().parseHex(parts[1]);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        digest.update(salt);

        byte[] calculated = digest.digest(
                password.getBytes(StandardCharsets.UTF_8)
        );

        return MessageDigest.isEqual(calculated, expected);
    }

    private byte[] derive(
            String password,
            byte[] salt,
            int iterations,
            int length
    ) {

        try {

            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    password.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            int hLen = mac.getMacLength();
            int blocks = (length + hLen - 1) / hLen;

            byte[] derived = new byte[length];

            for (int blockIndex = 1; blockIndex <= blocks; blockIndex++) {

                byte[] block = ByteBuffer.allocate(salt.length + 4)
                        .put(salt)
                        .putInt(blockIndex)
                        .array();

                byte[] u = mac.doFinal(block);
                byte[] xor = u.clone();

                for (int i = 1; i < iterations; i++) {

                    u = mac.doFinal(u);

                    for (int j = 0; j < hLen; j++) {
                        xor[j] ^= u[j];
                    }
                }

                int offset = (blockIndex - 1) * hLen;
                int count = Math.min(hLen, length - offset);

                System.arraycopy(xor, 0, derived, offset, count);
            }

            return derived;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error al generar el hash de contraseña", e
            );
        }
    }
}
