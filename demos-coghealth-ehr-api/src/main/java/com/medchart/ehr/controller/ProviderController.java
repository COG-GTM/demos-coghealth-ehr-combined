package com.medchart.ehr.controller;

import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/v1/providers")
@Validated
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public List<Provider> getAll(@RequestParam(required = false) Boolean active) {
        if (Boolean.TRUE.equals(active)) {
            return providerService.findActive();
        }
        return providerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Provider> getById(@PathVariable Long id) {
        return providerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npi/{npi}")
    public ResponseEntity<Provider> getByNpi(
            @PathVariable @Pattern(regexp = "\\d{10}", message = "NPI must be a 10-digit number") String npi) {
        return providerService.findByNpi(npi)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{department}")
    public List<Provider> getByDepartment(@PathVariable String department) {
        return providerService.findByDepartment(department);
    }

    @GetMapping("/specialty/{specialty}")
    public List<Provider> getBySpecialty(@PathVariable String specialty) {
        return providerService.findBySpecialty(specialty);
    }

    @GetMapping("/departments")
    public List<String> getDepartments() {
        return providerService.getAllDepartments();
    }

    @GetMapping("/specialties")
    public List<String> getSpecialties() {
        return providerService.getAllSpecialties();
    }

    @GetMapping("/search")
    public List<Provider> search(
            @RequestParam @NotBlank @Size(min = 1, max = 100, message = "lastName must be between 1 and 100 characters") String lastName) {
        return providerService.search(lastName);
    }

    @PostMapping
    public Provider create(@Valid @RequestBody Provider provider) {
        return providerService.save(provider);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Provider> update(@PathVariable Long id, @Valid @RequestBody Provider provider) {
        return providerService.findById(id)
                .map(existing -> {
                    provider.setId(id);
                    return ResponseEntity.ok(providerService.save(provider));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        providerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
