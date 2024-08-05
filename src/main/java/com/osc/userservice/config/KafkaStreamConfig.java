package com.osc.userservice.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Data
@Slf4j
@Configuration
public class KafkaStreamConfig {
    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;
    @Value("${kafka.application.id}")
    private String applicationId;
    @Value("${kafka.topic.forgotPassOtp}")
    private String forgotPassOtpTopic;
    @Value("${kafka.topic.userDataTopic}")
    private String userTopic;
    @Value("${kafka.topic.otpTopic}")
    private String otpTopic;

    @Bean
    public Properties kafkaStreamsProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);  // Disable the cache to reduce latency
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10);        // Commit every 10ms for lower latency
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2); // For strong consistency
        return props;
    }

    @Bean
    public KafkaStreams kafkaStreams() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        final Serde<String> stringSerde = Serdes.String();

        KTable<String, String> otpTable = streamsBuilder.table(otpTopic,
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("otp-KTable-Store")
                        .withKeySerde(stringSerde)
                        .withValueSerde(stringSerde));
        KTable<String, String> forgotPassOtpTable = streamsBuilder.table(forgotPassOtpTopic,
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("forgotpass-OTPKTable-Store")
                        .withKeySerde(stringSerde)
                        .withValueSerde(stringSerde));

        KTable<String, String> userDataTable = streamsBuilder.table(userTopic,
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("user-DATA-KTable-Store")
                        .withKeySerde(stringSerde)
                        .withValueSerde(stringSerde));
        userDataTable.toStream()
                .peek((key, value) -> log.info("Incoming UserData record - key: {}, value: {}", key, value));
        forgotPassOtpTable.toStream()
                .peek((key, value) -> log.info("Incoming ForgotPassOtp record - key: {}, value: {}", key, value));

        otpTable.toStream()
                .peek((key, value) -> log.info("Incoming record - key: {}, value: {}", key, value));

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), kafkaStreamsProperties());
        kafkaStreams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING) {
                log.info("Kafka Streams is running.");
            }
        });
        kafkaStreams.start();
        return kafkaStreams;
    }

    @Bean
    public ReadOnlyKeyValueStore<String, String> forgotPassOtpStore() {
        return kafkaStreams().store(StoreQueryParameters.fromNameAndType("forgotpass-OTPKTable-Store", QueryableStoreTypes.keyValueStore()));
    }

    @Bean
    public ReadOnlyKeyValueStore<String, String> userStore() {
        return kafkaStreams().store(StoreQueryParameters.fromNameAndType("user-DATA-KTable-Store", QueryableStoreTypes.keyValueStore()));
    }

    @Bean
    public ReadOnlyKeyValueStore<String, String> otpStore() {
        return kafkaStreams().store(StoreQueryParameters.fromNameAndType("otp-KTable-Store", QueryableStoreTypes.keyValueStore()));
    }
}
