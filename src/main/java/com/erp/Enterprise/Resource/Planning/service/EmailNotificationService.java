package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.dto.EmailResponse;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public EmailResponse sendPayslipEmail(String recipient, String subject, String body) {
        return sendEmail(recipient, subject, body, "Payslip email sent successfully.");
    }

    public EmailResponse sendPaymentApprovedEmail(String recipient, String subject, String body) {
        return sendEmail(recipient, subject, body, "Payment approval email sent successfully.");
    }

    public EmailResponse sendOtpEmail(String recipient, String subject, String body) {
        return sendEmail(recipient, subject, body, "Verification OTP email sent successfully.");
    }

    private EmailResponse sendEmail(String recipient, String subject, String body, String successMessage) {
        if (!StringUtils.hasText(fromAddress) || fromAddress.equals("your-email@gmail.com")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SMTP sender email is not configured in application.properties.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            return new EmailResponse(recipient, subject, successMessage);
        } catch (MailException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to send email. Check SMTP host, username, password, and network.");
        }
    }
}
