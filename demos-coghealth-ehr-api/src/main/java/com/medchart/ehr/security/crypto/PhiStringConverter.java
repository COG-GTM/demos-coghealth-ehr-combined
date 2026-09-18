package com.medchart.ehr.security.crypto;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Encrypts annotated String columns at rest with {@link PhiCipher}.
 *
 * <p>Hibernate may instantiate converters outside the Spring context, so the cipher is supplied by
 * {@link PhiEncryptionConfig} and resolved lazily on first use.
 */
@Converter
public class PhiStringConverter implements AttributeConverter<String, String> {

    private static volatile PhiCipher cipher;

    static void setCipher(PhiCipher phiCipher) {
        cipher = phiCipher;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return requireCipher().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return requireCipher().decrypt(dbData);
    }

    private PhiCipher requireCipher() {
        PhiCipher current = cipher;
        if (current == null) {
            throw new IllegalStateException("PhiCipher is not initialized; PHI column encryption unavailable");
        }
        return current;
    }
}
