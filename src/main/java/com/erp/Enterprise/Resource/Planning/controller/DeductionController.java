package com.erp.Enterprise.Resource.Planning.controller;

import com.erp.Enterprise.Resource.Planning.dto.DeductionRequest;
import com.erp.Enterprise.Resource.Planning.dto.DeductionResponse;
import com.erp.Enterprise.Resource.Planning.service.DeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deductions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class DeductionController {
    private final DeductionService deductionService;

    @GetMapping
    List<DeductionResponse> listDeductions() {
        return deductionService.listDeductions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DeductionResponse createDeduction(@Valid @RequestBody DeductionRequest request) {
        return deductionService.createDeduction(request);
    }

    @PutMapping("/{id}")
    DeductionResponse updateDeduction(@PathVariable Long id, @Valid @RequestBody DeductionRequest request) {
        return deductionService.updateDeduction(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDeduction(@PathVariable Long id) {
        deductionService.deleteDeduction(id);
    }
}
