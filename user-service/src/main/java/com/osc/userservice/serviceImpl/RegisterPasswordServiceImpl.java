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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterPasswordServiceImpl implements RegisterPasswordService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RegisterOtpServiceImpl registerOtpService;

    @Autowired
    public RegisterPasswordServiceImpl(UserMapper userMapper, UserRepository userRepository, RegisterOtpServiceImpl registerOtpService) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.registerOtpService = registerOtpService;
    }

    @Override
    public void createPassword(PasswordDTO passwordDTO) {

        String storedData = registerOtpService.getOtpStore(passwordDTO.getUserId());
        System.out.println("Stored data jjf" + storedData);
        if (storedData == null) {
            throw new PasswordException.UserNotFoundException("User not found");
        }

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




    }
}