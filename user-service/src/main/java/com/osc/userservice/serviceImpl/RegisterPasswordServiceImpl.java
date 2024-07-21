package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaStreamConfig;
import com.osc.userservice.dto.PasswordDTO;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.PasswordException;
import com.osc.userservice.mapper.UserMapper;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.RegisterPasswordService;
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
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class RegisterPasswordServiceImpl implements RegisterPasswordService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RegisterOtpServiceImpl registerOtpService;
    private final KafkaStreamConfig kafkaStreamConfig;
    private KafkaStreams kafkaStreams;
    private ReadOnlyKeyValueStore<String, String> userStore;

    @Value("${kafka.topic.userDataTopic}")
    private String userTopic;

    @Autowired
    public RegisterPasswordServiceImpl(UserMapper userMapper, UserRepository userRepository, RegisterOtpServiceImpl registerOtpService, KafkaStreamConfig kafkaStreamConfig) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.registerOtpService = registerOtpService;
        this.kafkaStreamConfig = kafkaStreamConfig;
    }

    @PostConstruct
    public void start() {
        try {
            StreamsBuilder streamsBuilder = configureStreamsBuilder();
            Properties streamsConfig = kafkaStreamConfig.kafkaStreamsProperties3();
            kafkaStreams = new KafkaStreams(streamsBuilder.build(), streamsConfig);
            initializeStreamsStateListener(kafkaStreams);
            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Error initializing Kafka Streams: {}", e.getMessage(), e);
        }
    }

    private StreamsBuilder configureStreamsBuilder() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final Serde<String> stringSerde = Serdes.String();
        try {
            KTable<String, String> otpDetailsTable = streamsBuilder.table(userTopic,
                    Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("user-DATA-KTable-Store")
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
                    userStore = streams.store(
                            StoreQueryParameters.fromNameAndType("user-DATA-KTable-Store", QueryableStoreTypes.keyValueStore())
                    );
                    System.out.println("userStore :::xyz"+userStore);
                    log.info("State store initialized and ready to use.");
                } catch (Exception e) {
                    log.error("Error initializing state store: {}", e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void createPassword(PasswordDTO passwordDTO) {
        // Fetch stored data from OTP service
        String storedData = userStore.get(passwordDTO.getUserId());

        // Check if stored data is null
        if (storedData == null) {
            throw new PasswordException.UserNotFoundException("User not found");
        }

        // Parse stored data to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode storedDataJson;
        try {
            storedDataJson = objectMapper.readTree(storedData);
        } catch (Exception e) {
            throw new PasswordException.PasswordCreationException("Error reading stored data");
        }

        String userId = storedDataJson.get("userId").asText();
        String email = storedDataJson.get("email").asText();
        String name = storedDataJson.get("name").asText();
        String contact = storedDataJson.get("contact").asText();
        String dob = storedDataJson.get("dob").asText();




        // Create User entity and set fields
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setName(name);
        user.setContact(contact);
        user.setDob(dob);
        user.setPassword(passwordDTO.getPassword());

        // Save user entity
        userRepository.save(user);
        log.info("User created with ID: {}", userId);
    }
}
