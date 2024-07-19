package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.config.KafkaStreamConfig;
import com.osc.userservice.dto.OtpTopicDto;
import com.osc.userservice.excetion.OtpExceptions;
import com.osc.userservice.mapper.UserMapper;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.RegisterOtpService;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

@Service
public class RegisterOtpServiceImpl implements RegisterOtpService {
    private KafkaProducerConfig kafkaProducerConfig;
    private final KafkaStreamConfig kafkaStreamConfig;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private KafkaStreams kafkaStreams;
    private ReadOnlyKeyValueStore<String, String> otpStore;
    private ReadOnlyKeyValueStore<String, Integer> attemptsStore;
    private static final Logger log = LoggerFactory.getLogger(RegisterOtpServiceImpl.class);
    private final Random random = new Random();
    private final Map<String, Integer> otpAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    @Value("${kafka.topic.otpTopic}")
    private String otpTopic;


    @Autowired
    public RegisterOtpServiceImpl(KafkaStreamConfig kafkaStreamConfig, UserMapper userMapper, UserRepository userRepository,KafkaProducerConfig kafkaProducerConfig) {
        this.kafkaStreamConfig = kafkaStreamConfig;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.kafkaProducerConfig=kafkaProducerConfig;
    }

    @PostConstruct
    public void startStream() {
        try {
            StreamsBuilder streamsBuilder = configureStreamsBuilder();// Kafka Streams application configure aur initialize, costom process.
            Properties streamsConfig = kafkaStreamConfig.kafkaStreamsProperties();
            kafkaStreams = new KafkaStreams(streamsBuilder.build(), streamsConfig);
            initializeStreamsStateListener(kafkaStreams);
            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Error initializing Kafka Streams: {}", e.getMessage(), e);
        }
    }

    private StreamsBuilder configureStreamsBuilder() {//streambuilder obj
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final Serde<String> stringSerde = Serdes.String();
        try {
            KTable<String, String> otpTable = streamsBuilder.table(otpTopic,
                    Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("otp-KTable-Store")
                            .withKeySerde(stringSerde)
                            .withValueSerde(stringSerde));

            otpTable.toStream()
                    .peek((key, value) -> log.info("Incoming record - key: {}, value: {}", key, value));

        } catch (Exception e) {
            log.error("Error configuring Kafka Streams builder: {}", e.getMessage(), e);
        }
        return streamsBuilder;
    }

    private void initializeStreamsStateListener(KafkaStreams streams) {
        streams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING) {
                try {
                    otpStore = streams.store(
                            StoreQueryParameters.fromNameAndType("otp-KTable-Store", QueryableStoreTypes.keyValueStore())
                    );

                    log.info("State store initialized and ready to use.");
                } catch (Exception e) {
                    log.error("Error initializing state store: {}", e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public void validateOtp(String userId, String otp) {
        String storedData = otpStore.get(userId);
        if (storedData == null) {
            throw new OtpExceptions.OtpValidationException("Invalid OTP");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode storedDataJson;
        try {
            storedDataJson = objectMapper.readTree(storedData);
        } catch (JsonProcessingException e) {
            throw new OtpExceptions.OtpValidationException("Error extracting OTP");
        }
        String storedOtp = storedDataJson.get("otp").asText();
        int attempts = storedDataJson.get("otpCount").asInt();

        if (!storedOtp.equals(otp)) {
            incrementOtpAttempts(userId, storedDataJson);
            throw new OtpExceptions.OtpValidationException("Invalid OTP");
        }
        if (attempts >= MAX_ATTEMPTS) {
            resetOtpAttempts(userId, storedDataJson);
            String newOtp = generateOtp();
            sendNewOtp(userId, newOtp, 0);
            throw new OtpExceptions.MaximumOtpAttemptsExceededException("Maximum OTP attempts exceeded. A new OTP has been sent.");
        }
        resetOtpAttempts(userId, storedDataJson);
    }


    @Override
    public String getOtpStore(String userId) {
        try {
            System.out.println("otp store " + otpStore.get(userId));
            return otpStore.get(userId);
        } catch (Exception e) {
            log.error("Error retrieving OTP store for userId {}: {}", userId, e.getMessage());
            return null;
        }
    }

    private void incrementOtpAttempts(String userId, JsonNode storedDataJson) {
        int attempts = storedDataJson.get("otpCount").asInt() + 1;
        sendNewOtp(userId, storedDataJson.get("otp").asText(), attempts);
    }

    private void resetOtpAttempts(String userId, JsonNode storedDataJson) {
        sendNewOtp(userId, storedDataJson.get("otp").asText(), 0);
    }


    private void sendNewOtp(String userId, String otp, int attempts) {
        OtpTopicDto otpTopicDto = new OtpTopicDto(userId, getEmailForUserId(userId), otp, attempts);

        ObjectMapper objectMapper = new ObjectMapper();
        String otpData;
        try {
            otpData = objectMapper.writeValueAsString(otpTopicDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing OTP JSON", e);
        }

        kafkaProducerConfig.sendMessage(otpTopic, userId, otpData);
    }

    private String getEmailForUserId(String userId) {
        try {
            String storedData = otpStore.get(userId);
            if (storedData == null) {
                throw new OtpExceptions.OtpValidationException("No OTP data found for userId: " + userId);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode storedDataJson = objectMapper.readTree(storedData);
            return storedDataJson.get("email").asText();
        } catch (Exception e) {
            log.error("Error retrieving email for userId {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error retrieving email for userId: " + userId, e);
        }
    }
}