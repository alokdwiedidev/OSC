package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaStreamConfig;
import com.osc.userservice.excetion.ForgotPasswordException;
import com.osc.userservice.excetion.OtpExceptions;
import com.osc.userservice.mapper.UserMapper;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.ForgotPasswordOtpService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

@Slf4j
@Service
public class ForgotPasswordOtpServiceImpl implements ForgotPasswordOtpService {
    private final KafkaStreamConfig kafkaStreamConfig;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private KafkaStreams streams;
    private ReadOnlyKeyValueStore<String, String> forgotPassotpStore;
    private final Random random = new Random();
    private final Map<String, Integer> otpAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    @Value("${kafka.topic.forgotPassOtp}")
    private String forgotPassOtpTopic;

    @Autowired
    public ForgotPasswordOtpServiceImpl(KafkaStreamConfig kafkaStreamConfig, UserMapper userMapper, UserRepository userRepository) {
        this.kafkaStreamConfig = kafkaStreamConfig;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void start() {
        try {
            StreamsBuilder streamsBuilder = configureStreamsBuilder();
            Properties streamsConfig = kafkaStreamConfig.kafkaStreamsPropertie2();
            streams = new KafkaStreams(streamsBuilder.build(), streamsConfig);
            initializeStreamsStateListener(streams);
            streams.start();
        } catch (Exception e) {
            log.error("Error initializing Kafka Streams: {}", e.getMessage(), e);
        }
    }

    private StreamsBuilder configureStreamsBuilder() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final Serde<String> stringSerde = Serdes.String();
        try {
            KTable<String, String> otpDetailsTable = streamsBuilder.table(forgotPassOtpTopic,
                    Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("forgotpass-OTPKTable-Store")
                            .withKeySerde(stringSerde)
                            .withValueSerde(stringSerde));

            otpDetailsTable.toStream()
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
                    forgotPassotpStore = streams.store(
                            StoreQueryParameters.fromNameAndType("forgotpass-OTPKTable-Store", QueryableStoreTypes.keyValueStore())
                    );
                    log.info("State store initialized and ready to use.");
                } catch (Exception e) {
                    log.error("Error initializing state store: {}", e.getMessage(), e);
                }
            }
        });
    }



    @Override
    public void validateOtp(String email, String otp) throws JsonProcessingException {
            if (otpAttempts.getOrDefault(email, 0) >= MAX_ATTEMPTS) {
                throw new OtpExceptions.MaximumOtpAttemptsExceededException("Maximum OTP attempts exceeded");
            }
            String storedData = forgotPassotpStore.get(email);
            if (storedData == null) {

                throw new ForgotPasswordException.OTPProcessingException("Invalid OTP");
            }

                // Deserializing the JSON string for extracting OTP
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode storedDataJson = objectMapper.readTree(storedData);
                String storedOtp = storedDataJson.get("otp").asText();
                if (!storedOtp.equals(otp)) {
                    incrementOtpAttempts(email);
                    throw new ForgotPasswordException.OTPProcessingException("OTP mismatch");

                }

                // Resetting the OTP attempts counter after a successful validation
                otpAttempts.remove(email);

                // Response



        }
    @Override
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void incrementOtpAttempts(String email) {
        otpAttempts.put(email, otpAttempts.getOrDefault(email, 0) + 1);
    }

}
