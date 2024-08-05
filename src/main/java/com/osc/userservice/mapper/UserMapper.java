package com.osc.userservice.mapper;

import com.osc.userservice.dto.UserTopicDto;
import com.osc.userservice.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private ModelMapper modelMapper;


    public User toEntityK(UserTopicDto userRegistrationMessage) {
        return modelMapper.map(userRegistrationMessage, User.class);
    }

}
