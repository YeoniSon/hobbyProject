package com.example.api.controller.user;

import com.example.api.security.CustomUserDetails;
import com.example.user.dto.request.*;
import com.example.user.dto.request.changePassword.ChangePasswordRequest;
import com.example.user.dto.request.changePassword.ChangeResetPasswordRequest;
import com.example.user.dto.request.changePassword.ResetPasswordRequest;
import com.example.user.dto.response.LoginResponse;
import com.example.user.dto.response.UserDataReponse;
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
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        String token = loginService.login(request);

        System.out.println(token);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // 회원정보 가져오기
    @GetMapping("/profile")
    public ResponseEntity<UserDataReponse> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getUserInfo(userDetails.getId()));
    }

    // 회원 정보 수정
    @PatchMapping("/profile/edit")
    public ResponseEntity<UserDataReponse> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(userDetails.getId(), request));
    }

    /*
    비밀번호 재설정 -> 로그인 되어있는 경우, 안되어있는 경우
    로그인 되어있는 경우 -> 변경가능
    안되어있는 경우 -> 토큰으로 인증 후 비밀번호 리셋, 변경
     */
    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(
                userDetails.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        System.out.println("비밀번호 변경 완료");
        return ResponseEntity.ok().build();
    }

    // 비밀번호 리셋
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    // 비밀번호 리셋 인증 (메일 링크 클릭 시 토큰 검증)
    @PostMapping("/reset-password/email-verify")
    public ResponseEntity<Void> passwordVerifyEmail(
            @RequestParam String token
    ) {
        emailVerificationService.verifyResetPasswordToken(token);
        return ResponseEntity.ok().build();
    }

    // 비밀번호 변경(로그인 x)
    @PostMapping("/reset-password/change-password")
    public ResponseEntity<Void> changeResetPassword(
            @RequestBody ChangeResetPasswordRequest request
    ) {
        userService.changeResetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        System.out.println("비밀번호 변경 완료");
        return ResponseEntity.ok().build();
    }

    //회원 탈퇴
    @PatchMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody WithdrawRequest request
    ){
        userService.userWithdraw(
                userDetails.getId(),
                request.getPassword()
        );

        return ResponseEntity.ok().build();
    }

    // 계정 복구
    @PatchMapping("/deposit")
    public ResponseEntity<Void> deposit(
            @RequestBody DepositRequest requset
    ){
        userService.userDeposit(
                requset.getEmail(),
                requset.getPassword()
        );

        return ResponseEntity.ok().build();
    }


}
