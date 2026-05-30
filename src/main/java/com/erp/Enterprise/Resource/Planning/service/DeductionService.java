package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.dto.DeductionRequest;
import com.erp.Enterprise.Resource.Planning.dto.DeductionResponse;
import com.erp.Enterprise.Resource.Planning.entity.Deduction;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import com.erp.Enterprise.Resource.Planning.repository.DeductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeductionService {
    private final DeductionRepository deductionRepository;

    @Transactional(readOnly = true)
    public List<DeductionResponse> listDeductions() {
        return deductionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public DeductionResponse createDeduction(DeductionRequest request) {
        if (deductionRepository.existsByNameIgnoreCase(request.name())) {
            throw new ApiException(HttpStatus.CONFLICT, "A deduction with this name already exists.");
        }
        Deduction deduction = new Deduction();
        apply(request, deduction);
        return toResponse(deductionRepository.save(deduction));
    }

    @Transactional
    public DeductionResponse updateDeduction(Long id, DeductionRequest request) {
        Deduction deduction = findDeduction(id);
        deductionRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "A deduction with this name already exists.");
                });
        apply(request, deduction);
        return toResponse(deduction);
    }

    @Transactional
    public void deleteDeduction(Long id) {
        deductionRepository.delete(findDeduction(id));
    }

    @Transactional(readOnly = true)
    public BigDecimal requiredActiveRate(String name) {
        Deduction deduction = deductionRepository.findByNameIgnoreCase(name)
                .filter(Deduction::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Missing active deduction rate: " + name));
        return deduction.getRatePercent();
    }

    private Deduction findDeduction(Long id) {
        return deductionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Deduction was not found."));
    }

    private void apply(DeductionRequest request, Deduction deduction) {
        deduction.setName(request.name().trim());
        deduction.setRatePercent(request.ratePercent());
        deduction.setActive(request.active() == null || request.active());
    }

    private DeductionResponse toResponse(Deduction deduction) {
        return new DeductionResponse(
                deduction.getId(),
                deduction.getName(),
                deduction.getRatePercent(),
                deduction.isActive()
        );
    }
}
