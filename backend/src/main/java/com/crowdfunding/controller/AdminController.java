package com.crowdfunding.controller;

import com.crowdfunding.dto.CampaignResponse;
import com.crowdfunding.dto.UserListResponse;
import com.crowdfunding.entity.User;
import com.crowdfunding.repository.WalletRepository;
import com.crowdfunding.service.CampaignService;
import com.crowdfunding.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final UserService userService;
    private final CampaignService campaignService;
    private final WalletRepository walletRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserListResponse>> getAllUsers() {
        List<UserListResponse> users = userService.findAllUsers().stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/block/{id}")
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        User user = userService.blockUser(id);
        return ResponseEntity.ok().body("User blocked: " + user.getUsername());
    }

    @PutMapping("/unblock/{id}")
    public ResponseEntity<?> unblockUser(@PathVariable Long id) {
        User user = userService.unblockUser(id);
        return ResponseEntity.ok().body("User unblocked: " + user.getUsername());
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns() {
        return ResponseEntity.ok(campaignService.getAllCampaignsForAdmin());
    }

    @DeleteMapping("/campaign/{id}")
    public ResponseEntity<?> cancelCampaign(@PathVariable Long id) {
        campaignService.cancelCampaign(id);
        return ResponseEntity.ok().body("Campaign cancelled in database. Call contract cancelCampaign from owner wallet if needed.");
    }

    private UserListResponse toUserResponse(User user) {
        UserListResponse r = new UserListResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setEmail(user.getEmail());
        r.setFullName(user.getFullName());
        r.setStatus(user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
        r.setRoles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()));
        r.setCreatedAt(user.getCreatedAt());
        walletRepository.findByUserId(user.getId()).ifPresent(w -> r.setWalletAddress(w.getAddress()));
        return r;
    }
}
