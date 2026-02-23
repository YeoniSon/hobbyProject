package com.example.user.dto.request;

import com.example.common.enums.Role;
import lombok.Getter;

@Getter
public class UpdateUserRequest {

    private String nickname;
    private String phone;
}
