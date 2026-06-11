package com.enigma.projectmtndew.controllers;

import com.enigma.projectmtndew.dtos.UserDTO;
import com.enigma.projectmtndew.entities.User;
import com.enigma.projectmtndew.repos.UserRepository;
import com.enigma.projectmtndew.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserService userService;

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> sync(@AuthenticationPrincipal Jwt jwt) {
        // Safely extract the subject UUID (Google/Supabase user ID)
        UUID userId = UUID.fromString(jwt.getSubject());

        // Safely fall back if claims are nested or named differently
        String email = jwt.getClaimAsString("email");

        // Google standard JWT maps full name to "name" instead of "username"
        String username = jwt.getClaimAsString("name");
        if (username == null && jwt.getClaim("user_metadata") != null) {
            Map<String, Object> userMetadata = jwt.getClaim("user_metadata");
            username = userMetadata.getOrDefault("username", userMetadata.getOrDefault("name", "New User")).toString();
        }
        if (username == null) {
            username = "User_" + userId.toString().substring(0, 6);
        }

        // Sync the account (Create if absent, otherwise update/retrieve)
        UserDTO synchronizedUser = userService.syncUser(userId, email, username);

        return ResponseEntity.ok(synchronizedUser);
    }
}
