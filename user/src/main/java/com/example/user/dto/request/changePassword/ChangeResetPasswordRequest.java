package com.example.user.dto.request.changePassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChangeResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
