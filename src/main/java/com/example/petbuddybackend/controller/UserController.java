package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get available user profiles")
    @GetMapping("/available-profiles")
    @PreAuthorize("isAuthenticated()")
    public UserProfiles getAvailableUserProfiles(Principal principal) {
        return userService.getUserProfiles(principal.getName());
    }

}
