package com.osc.userservice.service;

import com.osc.userservice.dto.PasswordDTO;

public interface RegisterPasswordService {
    void createPassword(PasswordDTO passwordDTO);
}
