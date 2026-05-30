package com.erp.Enterprise.Resource.Planning;

import com.erp.Enterprise.Resource.Planning.dto.PayrollRequest;
import com.erp.Enterprise.Resource.Planning.dto.PayslipResponse;
import com.erp.Enterprise.Resource.Planning.entity.PayslipStatus;
import com.erp.Enterprise.Resource.Planning.repository.MessageRepository;
import com.erp.Enterprise.Resource.Planning.service.PayrollService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PayrollServiceTests {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void processAndApprovePayrollComputesPayslipAndCreatesMessage() {
        PayrollRequest request = new PayrollRequest(6, 2025);

        List<PayslipResponse> pending = payrollService.processPayroll(request);
        PayslipResponse peter = findByEmployeeCode(pending, "EMP001");

        assertEquals(new BigDecimal("70000.00"), peter.baseSalary());
        assertEquals(new BigDecimal("9800.00"), peter.house());
        assertEquals(new BigDecimal("9800.00"), peter.transport());
        assertEquals(new BigDecimal("89600.00"), peter.grossSalary());
        assertEquals(new BigDecimal("21000.00"), peter.employeeTax());
        assertEquals(new BigDecimal("4200.00"), peter.pension());
        assertEquals(new BigDecimal("3500.00"), peter.medicalInsurance());
        assertEquals(new BigDecimal("3500.00"), peter.others());
        assertEquals(new BigDecimal("57400.00"), peter.netSalary());
        assertEquals(PayslipStatus.PENDING, peter.status());

        List<PayslipResponse> paid = payrollService.approvePayroll(request);
        PayslipResponse paidPeter = findByEmployeeCode(paid, "EMP001");

        assertEquals(PayslipStatus.PAID, paidPeter.status());
        var messages = messageRepository.findByEmployeeIdOrderByCreatedAtDesc(paidPeter.employeeId());
        assertFalse(messages.isEmpty());
        assertTrue(messages.get(0).getContent().contains("Dear Peter"));
        assertTrue(messages.get(0).getContent().contains("06/2025"));
    }

    private PayslipResponse findByEmployeeCode(List<PayslipResponse> payslips, String employeeCode) {
        return payslips.stream()
                .filter(payslip -> payslip.employeeCode().equals(employeeCode))
                .findFirst()
                .orElseThrow();
    }
}
