package ca.modmonster.minegit.data;

import ca.modmonster.minegit.MineGIT;
import net.fabricmc.loader.api.FabricLoader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoManager {
    public static final boolean AES_AVAILABLE;
    public static byte[] machineKey = null;

    static {
        boolean a;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            byte[] key = new byte[32];
            SecretKeySpec spec = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, spec);
            a = true;
        } catch (Exception e) {
            MineGIT.LOGGER.warn("AES encryption not available; encryption of stored PAT will be weaker");
            a = false;
        }
        AES_AVAILABLE = a;
    }

    public static String encrypt(String input) {
        if (AES_AVAILABLE) {
            try {
                SecretKeySpec key = new SecretKeySpec(getMachineKey(), "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, key);

                byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(encrypted);
            } catch (Exception e) {
                throw new RuntimeException("Encryption failed. RIP in peace", e);
            }
        } else {
            // basic XOR obfuscation
            byte[] key = getMachineKey();
            byte[] data = input.getBytes(StandardCharsets.UTF_8);

            for (int i = 0; i < data.length; i++) {
                data[i] ^= key[i % key.length];
            }

            return "x:" + Base64.getEncoder().encodeToString(data);
        }
    }

    public static String decrypt(String base64) {
        if (!base64.startsWith("x:")) {
            try {
                SecretKeySpec key = new SecretKeySpec(getMachineKey(), "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, key);

                byte[] decoded = Base64.getDecoder().decode(base64);
                return new String(cipher.doFinal(decoded));
            } catch (Exception e) {
                MineGIT.LOGGER.warn("Decryption of saved PAT failed. This could be due to hardware or environment changes, or runtime issues.");
                return null;
            }
        } else {
            // basic XOR obfuscation
            byte[] data = Base64.getDecoder().decode(base64.substring(2));
            byte[] key = getMachineKey();

            for (int i = 0; i < data.length; i++) {
                data[i] ^= key[i % key.length];
            }

            return new String(data, StandardCharsets.UTF_8);
        }
    }

    public static byte[] getMachineKey() {
        if (machineKey != null) return machineKey;

        String[] properties = {"os.arch", "user.name", "user.home", "sun.cpu.endian", "sun.cpu.isalist"};
        String[] environmentVariables = {"COMPUTERNAME", "PROCESSOR_ARCHITECTURE", "PROCESSOR_REVISION", "PROCESSOR_IDENTIFIER", "PROCESSOR_LEVEL", "NUMBER_OF_PROCESSORS", "OS", "USERNAME", "USERDOMAIN", "APPDATA", "HOMEPATH", "LOCALAPPDATA"};

        StringBuilder fingerprint = new StringBuilder();
        fingerprint.append(Runtime.getRuntime().availableProcessors());

        // Add system properties to fingerprint
        for (String property : properties) {
            fingerprint.append(System.getProperty(property));
        }

        // Add environment variables to fingerprint
        for (String var : environmentVariables) {
            fingerprint.append(System.getenv(var));
        }

        // Save a randomly generated key to a file
        Path keyFilePath = FabricLoader.getInstance().getConfigDir().resolve(".minegit.key");
        String fileKey = null;
        if (keyFilePath.toFile().exists()) {
            try {
                fileKey = new String(Files.readAllBytes(keyFilePath), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read from key file", e);
            }

            // Set file as hidden on Windows
            try {
                Files.setAttribute(keyFilePath, "dos:hidden", true);
            } catch (Exception ignored) {}
        } else {
            byte[] key = new byte[32];
            new SecureRandom().nextBytes(key);
            String encoded = Base64.getEncoder().encodeToString(key);
            try {
                Files.write(keyFilePath, encoded.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException("Failed to write to key file", e);
            }
        }
        if (fileKey == null) throw new RuntimeException("Something went wrong with the key file.");
        fingerprint.append(fileKey);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            machineKey = digest.digest(fingerprint.toString().getBytes(StandardCharsets.UTF_8));
            return machineKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available. Your Java runtime is fundamentally broken.", e);
        }
    }
}
