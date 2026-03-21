package com.example.api.controller.user;

import com.example.api.security.CustomUserDetails;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.request.admin.RoleChangeDto;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "JWTAuth")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 관리자 등록
    @Operation(security = {})
    @PostMapping("/register")
    public void register(
            @RequestBody SignUpRequest request
    ) {
        adminService.adminRegister(request);
    }

    // 계정 전체 조회
    @GetMapping("/manage/users")
    public ResponseEntity<List<UserDataReponse>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // 계정 삭제 처리 제외 전체 조회
    @GetMapping("/manage/users-without-withdraw")
    public ResponseEntity<List<UserDataReponse>> getAllUsersWithoutWithdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(adminService.getAllUsersWithoutWithdraw());
    }

    // 계정 삭제 처리 조회
    @GetMapping("/manage/withdraw-users")
    public ResponseEntity<List<UserDataReponse>> getAllWithdrawUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(adminService.getWithdrawUsers());
    }

    // 특정 계정 정보 조회
    @GetMapping("/manage/{userId}")
    public ResponseEntity<UserDataReponse> getUserById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    // 특정 계정 역할 변경
    @PatchMapping("/manage/{userId}/role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @RequestBody RoleChangeDto roleChangeDto
    ) {
        adminService.changeRole(userId, roleChangeDto.getRole());
        return ResponseEntity.ok().build();
    }

    // 특정 계정 삭제처리
    @PatchMapping("/manage/{userId}/withdraw")
    public ResponseEntity<Void> adminWithdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ){
        adminService.withdrawUser(userId);
        return ResponseEntity.ok().build();
    }

    // 특정 계정 복구처리
    @PatchMapping("/manage/{userId}/deposit")
    public ResponseEntity<Void> adminDeposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ){
        adminService.depositUser(userId);
        return ResponseEntity.ok().build();
    }
}
