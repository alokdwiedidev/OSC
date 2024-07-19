package com.osc.emailservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSenderImpl javaMailSenderImpl;
    private final String fromEmail = "alokdwivedi1255@gmail.com";
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public EmailService(JavaMailSenderImpl javaMailSenderImpl, KafkaConsumer<String, String> kafkaConsumer) {
        this.javaMailSenderImpl = javaMailSenderImpl;
        this.kafkaConsumer = kafkaConsumer;
        this.kafkaConsumer.subscribe(Arrays.asList("otpTopic", "forgotPassOtp"));

        start();
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        processMessage(record.topic(), record.value());
                    }
                }
            } catch (Exception e) {
                log.error("Error while polling messages from Kafka: ", e);
            }
        }).start();
    }

    public void processMessage(String topic, String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            if (topic.equals("otpTopic")) {
                String userId = jsonNode.get("userId").asText();
                String otp = jsonNode.get("otp").asText();
                String userEmail = jsonNode.get("email").asText();
                int otpCount = jsonNode.get("otpCount").asInt();

                if (otpCount == 0) {
                    sendOTPByEmail(userId, otp, userEmail);
                    log.info("OTP email sent for UserID: {}", userId);
                } else {
                    log.info("OTP email not sent because otpCount is not 0 for UserID: {}", userId);
                }
            } else if (topic.equals("forgotPassOtp")) {
                String otp = jsonNode.get("otp").asText();
                String userEmail = jsonNode.get("email").asText();
                sendForgotPasswordOTPEmail(otp, userEmail);
                log.info("Forgot Password OTP email sent to: " + userEmail);
            }

        } catch (Exception e) {
            log.error("Error processing User JSON Object from Kafka topic: ", e);
        }
    }

    private void sendOTPByEmail(String userId, String otp, String userEmail) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(userEmail);
            mailMessage.setSubject("OTP for Registration on OSC");
            mailMessage.setText("Your UserId is: " + userId + "\nYour OTP is: " + otp);
            javaMailSenderImpl.send(mailMessage);
            log.info("OTP email sent to: " + userEmail);
        } catch (Exception e) {
            log.error("Error sending OTP email: {}", e.getMessage());
        }
    }

    private void sendForgotPasswordOTPEmail(String otp, String userEmail) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(userEmail);
            mailMessage.setSubject("OTP for Password Reset on OSC");
            mailMessage.setText("Your OTP for password reset is: " + otp);
            javaMailSenderImpl.send(mailMessage);
            log.info("Forgot Password OTP email sent to: " + userEmail);
        } catch (Exception e) {
            log.error("Error sending Forgot Password OTP email: {}", e.getMessage());
        }
    }
}
