package com.osc.userservice.serviceImpl;

import com.osc.userservice.dto.ChangePasswordRequestDTO;
import com.osc.userservice.entity.User;
import com.osc.userservice.excetion.ForgotPasswordException;
import com.osc.userservice.repository.UserRepository;
import com.osc.userservice.responce.ApiResponse;
import com.osc.userservice.service.PasswordChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PasswordChangeServiceImpl implements PasswordChangeService {
    @Autowired
    private UserRepository userRepository;


    @Override
    public void changePassword(ChangePasswordRequestDTO changePasswordRequestDTO) {

        String email = changePasswordRequestDTO.getEmail();
        String newPassword = changePasswordRequestDTO.getPassword();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.warn("User not found with email: {}", email);
            throw new ForgotPasswordException.UserNotFoundException("User not found with email: " + email);
        }
        User user = userOptional.get();
        user.setPassword(newPassword);
        userRepository.save(user);




    }
}