package com.medchart.ehr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Single storage gateway for exported patient data (PHI).
 *
 * <p>Exports never leave a private, owner-only directory: the destination is confined to a
 * configured root, files are created with {@code rw-------} before any content is written, and the
 * payload is encrypted at rest with AES-256-GCM when {@code medchart.export.encryption-key} is set.
 * Callers only ever receive an opaque handle, never a filesystem path.
 */
@Service
@Slf4j
public class PhiExportStorage {

    private static final Pattern SAFE_HANDLE = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,127}");
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS =
            PosixFilePermissions.fromString("rwx------");
    private static final Set<PosixFilePermission> FILE_PERMISSIONS =
            PosixFilePermissions.fromString("rw-------");
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private final Path root;
    private final SecretKey encryptionKey;
    private final SecureRandom random = new SecureRandom();

    public PhiExportStorage(
            @Value("${medchart.export.storage-dir:}") String storageDir,
            @Value("${medchart.export.encryption-key:}") String encryptionKey) {
        this.root = (storageDir == null || storageDir.trim().isEmpty())
                ? Paths.get(System.getProperty("user.home"), ".medchart", "phi-exports")
                : Paths.get(storageDir);
        this.encryptionKey = readKey(encryptionKey);
    }

    @PostConstruct
    void prepareRoot() {
        try {
            Files.createDirectories(root);
            restrictPermissions(root, DIRECTORY_PERMISSIONS);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to prepare PHI export directory " + root, e);
        }
        if (encryptionKey == null) {
            log.warn("PHI exports in {} are not encrypted at rest: medchart.export.encryption-key is not configured",
                    root);
        }
    }

    /**
     * Stores {@code content} under a private, owner-only file and returns its opaque handle.
     */
    public String store(String handle, byte[] content) {
        Path target = resolve(handle);
        try {
            Files.deleteIfExists(target);
            Files.createFile(target, PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS));
            restrictPermissions(target, FILE_PERMISSIONS);
            Files.write(target, encrypt(content));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store PHI export " + handle, e);
        }
        log.info("Stored PHI export {} ({} bytes, encrypted={}) in private storage", handle, content.length,
                encryptionKey != null);
        return handle;
    }

    /**
     * Reads back a stored export and removes it, so PHI is not left behind after delivery.
     */
    public byte[] retrieveAndDelete(String handle) {
        Path target = resolve(handle);
        try {
            byte[] stored = Files.readAllBytes(target);
            return decrypt(stored);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read PHI export " + handle, e);
        } finally {
            try {
                Files.deleteIfExists(target);
            } catch (IOException e) {
                log.warn("Could not delete PHI export {} after delivery", handle, e);
            }
        }
    }

    private Path resolve(String handle) {
        if (handle == null || !SAFE_HANDLE.matcher(handle).matches()) {
            throw new IllegalArgumentException("Invalid export handle");
        }
        Path target = root.resolve(handle).normalize();
        if (!target.getParent().equals(root.normalize())) {
            throw new IllegalArgumentException("Invalid export handle");
        }
        return target;
    }

    private void restrictPermissions(Path path, Set<PosixFilePermission> permissions) throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if (view != null) {
            view.setPermissions(permissions);
        }
    }

    private SecretKey readKey(String configured) {
        if (configured == null || configured.trim().isEmpty()) {
            return null;
        }
        byte[] key = Base64.getDecoder().decode(configured.trim());
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalStateException("medchart.export.encryption-key must be a base64 AES key");
        }
        return new SecretKeySpec(key, "AES");
    }

    private byte[] encrypt(byte[] plaintext) {
        if (encryptionKey == null) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return ByteBuffer.allocate(iv.length + ciphertext.length).put(iv).put(ciphertext).array();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt PHI export", e);
        }
    }

    private byte[] decrypt(byte[] stored) {
        if (encryptionKey == null) {
            return stored;
        }
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey,
                    new GCMParameterSpec(GCM_TAG_BITS, stored, 0, GCM_IV_BYTES));
            return cipher.doFinal(stored, GCM_IV_BYTES, stored.length - GCM_IV_BYTES);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt PHI export", e);
        }
    }
}
