package com.osc.sessionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.sessionservice.config.KafkaConfig;
import com.osc.sessionservice.entity.SessionEntity;
import com.osc.sessionservice.repository.SessionRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

@Slf4j
@Service
public class SessionUpdate {

    private static final Logger logger = LoggerFactory.getLogger(SessionUpdate.class);

    private ReadOnlyKeyValueStore<String, String> sessionStore;
    private KafkaStreams kafkaStreams;
    private SessionRepository sessionRepository;
    private KafkaConfig kafkaConfig;

    @Autowired
    public SessionUpdate(SessionRepository sessionRepository, KafkaConfig kafkaConfig) {
        this.sessionRepository = sessionRepository;
        this.kafkaConfig = kafkaConfig;
    }

    @PostConstruct
    public void start() {
        try {
            createAndStartStream();
        } catch (Exception e) {
            log.error("Error initializing Kafka Streams: {}", e.getMessage(), e);
        }
    }

    private void createAndStartStream() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final Serde<String> stringSerde = Serdes.String();
        try {
            KTable<String, String> userDetailsTable = streamsBuilder.table("session",
                    Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("session-ktable")
                            .withKeySerde(stringSerde)
                            .withValueSerde(stringSerde));

            userDetailsTable.toStream()
                    .foreach(this::processSessionUpdate);

            Properties streamsConfig = kafkaConfig.kafkaStreamsProperties();
            kafkaStreams = new KafkaStreams(streamsBuilder.build(), streamsConfig);
            initializeStreamsStateListener(kafkaStreams);
            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Error configuring or starting Kafka Streams: {}", e.getMessage(), e);
        }
    }

    private void initializeStreamsStateListener(KafkaStreams streams) {
        streams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING) {
                try {
                    sessionStore = streams.store(
                            StoreQueryParameters.fromNameAndType("session-ktable", QueryableStoreTypes.keyValueStore())
                    );
                    log.info("State store initialized and ready to use.");
                } catch (Exception e) {
                    log.error("Error initializing state store: {}", e.getMessage(), e);
                }
            }
        });
    }

    private void processSessionUpdate(String key, String value) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(value);
            String userId=jsonNode.get("userId").asText();
            String sessionId = jsonNode.get("sessionId").asText();
            String action = jsonNode.get("action").asText();
            String device = jsonNode.get("device").asText();
            System.out.println("devicealokka"+device);

            SessionEntity sessionEntity = sessionRepository.findByUserId(userId);

            if (sessionEntity == null) {
                sessionEntity = new SessionEntity();
                sessionEntity.setDevice(device);
                sessionEntity.setUserId(userId);
                logger.info("New session entity created for userId: {}", userId);
            }

            if ("logout".equals(action)) {
                sessionEntity.setLogoutTime(LocalDateTime.now());
                sessionEntity.setDevice(null);
                sessionEntity.setSessionId(null);
                sessionEntity.setLoginTime(null);
                logger.info("User logged out for session: {}", sessionId);
            } else {
                sessionEntity.setSessionId(sessionId);
                sessionEntity.setLoginTime(LocalDateTime.now());
                sessionEntity.setDevice(device);
                sessionEntity.setLogoutTime(null);
                logger.info("Session logged in data updated in the database for session: {}", sessionId);
            }

            sessionRepository.save(sessionEntity);

        } catch (IOException e) {
            logger.error("Error processing message for key {}: {}", key, e.getMessage());
        }
    }}