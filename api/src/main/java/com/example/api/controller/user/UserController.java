package com.example.api.controller.user;

import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.request.UpdateUserRequest;
import com.example.user.dto.response.LoginResponse;
import com.example.user.service.EmailVerificationService;
import com.example.user.service.LoginService;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginService loginService;
    private final EmailVerificationService emailVerificationService;

    // 회원가입
    @PostMapping("/signup")
    public void signUp(@RequestBody SignUpRequest request) {
        userService.signUp(request);
    }

    //이메일 인증
    @GetMapping("/email-verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyToken(token);
        System.out.println("이메일 인증 완료");
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = loginService.login(request);

        System.out.println(token);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
