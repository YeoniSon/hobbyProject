package com.example.api.controller.user;

import com.example.api.security.CustomUserDetails;
import com.example.user.dto.request.*;
import com.example.user.dto.request.changePassword.ChangePasswordRequest;
import com.example.user.dto.request.changePassword.ChangeResetPasswordRequest;
import com.example.user.dto.request.changePassword.ResetPasswordRequest;
import com.example.user.dto.response.LoginResponse;
import com.example.user.dto.response.SignupResponse;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.service.EmailVerificationService;
import com.example.user.service.LoginService;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "사용자", description = "회원가입, 로그인, 프로필, 비밀번호, 탈퇴·복구")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginService loginService;
    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "회원가입", description = "이메일 인증을 포함한 계정 생성 요청을 보냅니다.", security = {})
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signUp(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(userService.signUp(request));
    }

    @Operation(summary = "이메일 인증", description = "가입 메일의 토큰으로 이메일 검증을 완료합니다.", security = {})
    @GetMapping("/email-verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyToken(token);
        System.out.println("이메일 인증 완료");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하고 JWT를 발급받습니다.", security = {})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        String token = loginService.login(request);

        System.out.println(token);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.", security = @SecurityRequirement(name = "JWTAuth"))
    @GetMapping("/profile")
    public ResponseEntity<UserDataReponse> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getUserInfo(userDetails.getId()));
    }

    @Operation(summary = "프로필 수정", description = "닉네임 등 프로필 정보를 수정합니다.", security = @SecurityRequirement(name = "JWTAuth"))
    @PatchMapping("/profile/edit")
    public ResponseEntity<UserDataReponse> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(userDetails.getId(), request));
    }

    @Operation(summary = "비밀번호 변경 (로그인 상태)", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.", security = @SecurityRequirement(name = "JWTAuth"))
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

    @Operation(summary = "비밀번호 재설정 메일 요청", description = "이메일로 비밀번호 재설정 링크를 보냅니다.", security = {})
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정 토큰 검증", description = "메일 링크의 토큰 유효성을 검증합니다.", security = {})
    @PostMapping("/reset-password/email-verify")
    public ResponseEntity<Void> passwordVerifyEmail(
            @RequestParam String token
    ) {
        emailVerificationService.verifyResetPasswordToken(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경 (비로그인)", description = "재설정 토큰과 새 비밀번호로 변경합니다.", security = {})
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

    @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 계정을 탈퇴 처리합니다.", security = @SecurityRequirement(name = "JWTAuth"))
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

    @Operation(summary = "계정 복구", description = "탈퇴한 계정을 이메일·비밀번호로 복구합니다.", security = @SecurityRequirement(name = "JWTAuth"))
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
