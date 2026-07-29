package com.medchart.ehr.service;

import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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

    private Provider provider() {
        return Provider.builder()
                .id(1L)
                .npi("1234567890")
                .firstName("Sarah")
                .lastName("Anderson")
                .department("Cardiology")
                .specialty("Interventional Cardiology")
                .active(true)
                .build();
    }

    @Test
    void findActiveReturnsOnlyActiveProviders() {
        Provider active = provider();
        when(providerRepository.findByActiveTrue()).thenReturn(Collections.singletonList(active));

        assertThat(providerService.findActive()).containsExactly(active);
    }

    @Test
    void findByNpiDelegatesToRepository() {
        Provider provider = provider();
        when(providerRepository.findByNpi("1234567890")).thenReturn(Optional.of(provider));

        assertThat(providerService.findByNpi("1234567890")).contains(provider);
    }

    @Test
    void searchMatchesPartialLastName() {
        Provider provider = provider();
        when(providerRepository.findByLastNameContainingIgnoreCase("and"))
                .thenReturn(Collections.singletonList(provider));

        assertThat(providerService.search("and")).containsExactly(provider);
    }

    @Test
    void getAllDepartmentsDelegatesToRepository() {
        when(providerRepository.findAllDepartments())
                .thenReturn(Arrays.asList("Cardiology", "Oncology"));

        assertThat(providerService.getAllDepartments()).containsExactly("Cardiology", "Oncology");
    }

    @Test
    void deactivateClearsActiveFlagAndSaves() {
        Provider provider = provider();
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        providerService.deactivate(1L);

        assertThat(provider.getActive()).isFalse();
        verify(providerRepository).save(provider);
    }

    @Test
    void deactivateIsNoOpWhenProviderIsMissing() {
        when(providerRepository.findById(404L)).thenReturn(Optional.empty());

        providerService.deactivate(404L);

        verify(providerRepository, never()).save(any());
    }
}
