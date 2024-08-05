package com.osc.userservice.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.dto.PasswordDTO;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.PasswordException;
import com.osc.userservice.mapper.UserMapper;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.RegisterPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterPasswordServiceImpl implements RegisterPasswordService {
    private final UserRepository userRepository;
    private final ReadOnlyKeyValueStore<String, String> userStore;

    @Autowired
    public RegisterPasswordServiceImpl( UserRepository userRepository, ReadOnlyKeyValueStore<String, String> userStore) {
        this.userRepository = userRepository;
        this.userStore = userStore;
    }

    @Override
    public void createPassword(PasswordDTO passwordDTO) {

        String storedData = userStore.get(passwordDTO.getUserId());
        if (storedData == null) {
            throw new PasswordException.UserNotFoundException("User not found");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode storedDataJson;
        try {
            storedDataJson = objectMapper.readTree(storedData);
        } catch (Exception e) {
            log.error("Error reading stored data for userId: {}", passwordDTO.getUserId(), e);
            throw new PasswordException.PasswordCreationException("Internal server error");
        }

        String userId = storedDataJson.get("userId").asText();
        String email = storedDataJson.get("email").asText();
        String name = storedDataJson.get("name").asText();
        String contact = storedDataJson.get("contact").asText();
        String dob = storedDataJson.get("dob").asText();

        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setName(name);
        user.setContact(contact);
        user.setDob(dob);
        user.setPassword(passwordDTO.getPassword());

        userRepository.save(user);
        log.info("User created with ID: {}", userId);
    }
}
