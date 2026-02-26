package com.example.user.dto.response;

import com.example.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CodePointBuffer;

import java.time.LocalDate;

@Getter
@Builder
public class UserDataReponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String nickname;
    private LocalDate birth;
    private boolean deleted;

    public static UserDataReponse from(User user) {
        return UserDataReponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .birth(user.getBirth())
                .deleted(user.isDeleted())
                .build();
    }
}
