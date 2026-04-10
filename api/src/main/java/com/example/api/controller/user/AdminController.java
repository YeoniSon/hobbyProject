package com.example.api.controller.user;

import com.example.api.security.CustomUserDetails;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.request.admin.RoleChangeDto;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "관리자", description = "관리자 계정·회원 관리 (대부분 JWT 필요)")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "관리자 등록", description = "초기 관리자 계정 등록(공개). 운영 시 보안 정책에 맞게 제한하세요.", security = {})
    @PostMapping("/register")
    public void register(
            @RequestBody SignUpRequest request
    ) {
        adminService.adminRegister(request);
    }

    @Operation(summary = "전체 회원 조회", description = "모든 회원 목록을 조회합니다.")
    @GetMapping("/manage/users")
    public ResponseEntity<List<UserDataReponse>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(summary = "탈퇴 제외 회원 조회", description = "탈퇴 처리되지 않은 회원만 조회합니다.")
    @GetMapping("/manage/users-without-withdraw")
    public ResponseEntity<List<UserDataReponse>> getAllUsersWithoutWithdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(adminService.getAllUsersWithoutWithdraw());
    }

    @Operation(summary = "탈퇴 회원 조회", description = "탈퇴 처리된 회원만 조회합니다.")
    @GetMapping("/manage/withdraw-users")
    public ResponseEntity<List<UserDataReponse>> getAllWithdrawUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(adminService.getWithdrawUsers());
    }

    @Operation(summary = "회원 단건 조회", description = "userId로 회원 상세를 조회합니다.")
    @GetMapping("/manage/{userId}")
    public ResponseEntity<UserDataReponse> getUserById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @Operation(summary = "회원 역할 변경", description = "회원의 역할(권한)을 변경합니다.")
    @PatchMapping("/manage/{userId}/role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @RequestBody RoleChangeDto roleChangeDto
    ) {
        adminService.changeRole(userId, roleChangeDto.getRole());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴 처리", description = "관리자가 해당 회원을 탈퇴 처리합니다.")
    @PatchMapping("/manage/{userId}/withdraw")
    public ResponseEntity<Void> adminWithdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ){
        adminService.withdrawUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 복구 처리", description = "탈퇴한 회원을 복구합니다.")
    @PatchMapping("/manage/{userId}/deposit")
    public ResponseEntity<Void> adminDeposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ){
        adminService.depositUser(userId);
        return ResponseEntity.ok().build();
    }
}
