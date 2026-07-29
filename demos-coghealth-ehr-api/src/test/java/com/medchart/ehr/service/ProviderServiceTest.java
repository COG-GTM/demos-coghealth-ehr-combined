package com.medchart.ehr.service;

import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderService providerService;

    private Provider provider;

    @BeforeEach
    void setUp() {
        provider = new Provider();
        provider.setId(7L);
        provider.setNpi("1234567890");
        provider.setFirstName("Alice");
        provider.setLastName("Nguyen");
        provider.setActive(true);
    }

    @Test
    void findActive_returnsOnlyActiveProviders() {
        when(providerRepository.findByActiveTrue()).thenReturn(Collections.singletonList(provider));

        assertThat(providerService.findActive()).containsExactly(provider);
    }

    @Test
    void findByNpi_delegatesToRepository() {
        when(providerRepository.findByNpi("1234567890")).thenReturn(Optional.of(provider));

        assertThat(providerService.findByNpi("1234567890")).contains(provider);
    }

    @Test
    void search_matchesOnLastName() {
        when(providerRepository.findByLastNameContainingIgnoreCase("ngu"))
                .thenReturn(Collections.singletonList(provider));

        assertThat(providerService.search("ngu")).containsExactly(provider);
    }

    @Test
    void deactivate_clearsActiveFlagAndSaves() {
        when(providerRepository.findById(7L)).thenReturn(Optional.of(provider));

        providerService.deactivate(7L);

        assertThat(provider.getActive()).isFalse();
        verify(providerRepository).save(provider);
    }

    @Test
    void deactivate_isNoOpWhenProviderMissing() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());

        providerService.deactivate(99L);

        verify(providerRepository, never()).save(any());
    }
}
