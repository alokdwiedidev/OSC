package com.osc.userservice.serviceImpl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osc.userservice.config.KafkaProducerConfig;
import com.osc.userservice.dto.OtpTopicDto;
import com.osc.userservice.dto.RegisterUserDto;
import com.osc.userservice.dto.UserTopicDto;
import com.osc.userservice.excetion.JsonProcessingCustomException;
import com.osc.userservice.excetion.UserAlreadyExistsException;
import com.osc.userservice.mapper.UserMapper;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.service.RegisterOtpService;
import com.osc.userservice.service.RegisterUserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class RegisterUserServiceImpl implements RegisterUserService {
    private UserRepository userRepository;
    private UserMapper userMapper;
    private RegisterOtpService otpService;
    private KafkaProducerConfig kafkaConfig;
    private ModelMapper modelMapper;
    @Value("${kafka.topic.userDataTopic}")
    private String USERTOPIC;
    @Value("${kafka.topic.otpTopic}")
    private String OTPTOPIC;

    @Autowired
    public RegisterUserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, RegisterOtpService otpService, UserMapper userMapper, KafkaProducerConfig kafkaConfig) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.otpService = otpService;
        this.userMapper = userMapper;
        this.kafkaConfig = kafkaConfig;
    }


    @Override
    public String registerUser(RegisterUserDto userRegistrationDTO) throws JsonProcessingCustomException {

        // Check if email already exists
        if (userRepository.findByEmail(userRegistrationDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(" user allready exist");
        }

        // Generate user ID and OTP
        String userId = generateUserId(userRegistrationDTO.getName());
        String otp = otpService.generateOtp();


        // Preparing message entity for Kafka
        UserTopicDto userTopicDto = modelMapper.map(userRegistrationDTO, UserTopicDto.class);
        userTopicDto.setUserId(userId);

        OtpTopicDto otpTopicDto = new OtpTopicDto(userId, userRegistrationDTO.getEmail(), otp, 0);

        // Converting message entity to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String userData;
        String otpData;
        try {
            userData = objectMapper.writeValueAsString(userTopicDto);
            otpData = objectMapper.writeValueAsString(otpTopicDto);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException("Error processing user registration JSON");
        }
        // Send registration data to Kafka
        kafkaConfig.sendMessage(USERTOPIC, userId, userData);
        kafkaConfig.sendMessage(OTPTOPIC,userId,otpData);
        //Response
        return userId;
    }


    public String generateUserId(String userName) {
        Random random = new Random();
        int randomInt = random.nextInt(10000);
        String cleanedUsername = userName.replaceAll("\\s", "");


        return cleanedUsername + randomInt;
    }
}