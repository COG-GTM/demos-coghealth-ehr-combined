package com.medchart.ehr.service;

import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    private ProviderService providerService;

    private Provider provider;

    @BeforeEach
    void setUp() {
        providerService = new ProviderService(providerRepository);
        provider = Provider.builder()
                .id(3L)
                .npi("1234567893")
                .firstName("Grace")
                .lastName("Hopper")
                .department("Cardiology")
                .specialty("Interventional Cardiology")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("lookups delegate to the matching repository query")
    void lookupsDelegateToRepository() {
        when(providerRepository.findById(3L)).thenReturn(Optional.of(provider));
        when(providerRepository.findByNpi("1234567893")).thenReturn(Optional.of(provider));
        when(providerRepository.findByActiveTrue()).thenReturn(List.of(provider));
        when(providerRepository.findByDepartment("Cardiology")).thenReturn(List.of(provider));
        when(providerRepository.findBySpecialty("Interventional Cardiology")).thenReturn(List.of(provider));
        when(providerRepository.findByLastNameContainingIgnoreCase("hop")).thenReturn(List.of(provider));

        assertThat(providerService.findById(3L)).contains(provider);
        assertThat(providerService.findByNpi("1234567893")).contains(provider);
        assertThat(providerService.findActive()).containsExactly(provider);
        assertThat(providerService.findByDepartment("Cardiology")).containsExactly(provider);
        assertThat(providerService.findBySpecialty("Interventional Cardiology")).containsExactly(provider);
        assertThat(providerService.search("hop")).containsExactly(provider);
    }

    @Test
    @DisplayName("deactivate clears the active flag and saves the provider")
    void deactivateClearsActiveFlag() {
        when(providerRepository.findById(3L)).thenReturn(Optional.of(provider));

        providerService.deactivate(3L);

        assertThat(provider.getActive()).isFalse();
        verify(providerRepository).save(provider);
    }

    @Test
    @DisplayName("deactivate is a no-op for an unknown provider")
    void deactivateIgnoresMissingProvider() {
        when(providerRepository.findById(404L)).thenReturn(Optional.empty());

        providerService.deactivate(404L);

        verify(providerRepository, never()).save(any());
    }

    @Test
    @DisplayName("save returns the persisted provider")
    void saveReturnsPersistedProvider() {
        when(providerRepository.save(provider)).thenReturn(provider);

        assertThat(providerService.save(provider)).isSameAs(provider);
    }
}
