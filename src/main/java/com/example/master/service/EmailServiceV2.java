package com.example.master.service;

import com.example.master.dtobj.event.DemandCreatedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class EmailServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceV2.class);
    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from.address}")
    private String fromAddress;

    @Value("${mail.from.name}")
    private String fromName;

    private final ObjectMapper mapper;

    public EmailServiceV2(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void sendTemplateEmail(String to, String templateName, Map<String, String> values) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("templates/mail.json");
            JsonNode templates = mapper.readTree(is);

            JsonNode template = templates.get(templateName);
            if (template == null) throw new IllegalArgumentException("Template not found: " + templateName);

            String subject = inject(template.get("subject").asText(), values);
            String body = inject(template.get("body").asText(), values) + "\n\n" + inject(template.get("footer").asText(), values);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // plain text

            mailSender.send(message);
        }  catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String inject(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }

    public void sendSimpleMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.format("%s <%s>", fromName, fromAddress)); // "SNP-THR <your-email>"
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendEmailNotification(DemandCreatedEvent event) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo("recipient@example.com");
            helper.setSubject("New Demand Created");
            helper.setText("<h3>New Demand Created</h3><p>AWC: " + event.getAwc() +  "District: " + event.getDistrict() + "\n" +
                    "Items: " + event.getSummary()+ "</p>", true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            log.error("Failed to send email for demand ID: {}. Reason: {}", event.getDemandId(), ex.getMessage(), ex);
        }
    }
}
