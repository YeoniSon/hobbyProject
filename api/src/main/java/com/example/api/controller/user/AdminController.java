package com.example.api.controller.user;

import com.example.api.security.CustomUserDetails;
import com.example.user.dto.request.SignUpRequest;
import com.example.user.dto.request.admin.RoleChangeDto;
import com.example.user.dto.response.UserDataReponse;
import com.example.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public void register(
            @RequestBody SignUpRequest request
    ) {
        adminService.adminRegister(request);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDataReponse>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDataReponse> getUserById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @RequestBody RoleChangeDto roleChangeDto
    ) {
        adminService.changeRole(userId, roleChangeDto.getRole());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/withdraw")
    public ResponseEntity<Void> adminWithdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ){
        adminService.withdrawUser(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/deposit")
    public ResponseEntity<Void> adminDeposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ){
        adminService.depositUser(userId);
        return ResponseEntity.ok().build();
    }
}
