package com.medchart.ehr.security.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class PhiEncryptionConfig {

    private final PhiCipher phiCipher;

    @PostConstruct
    void registerConverterCipher() {
        PhiStringConverter.setCipher(phiCipher);
    }
}
