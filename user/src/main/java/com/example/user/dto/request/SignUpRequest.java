package com.example.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    private String email;
    private String name;
    private String nickname;
    private String password;
    private String phone;
    private LocalDate birth;

}
