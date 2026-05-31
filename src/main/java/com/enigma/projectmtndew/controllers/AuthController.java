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
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserService userService;

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> sync(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString((jwt.getSubject()));

        String email =  jwt.getClaims().get("email").toString();
        String username = jwt.getClaims().get("username").toString();

        UserDTO createdUser = userService.saveUser(userId, email, username);

        return ResponseEntity.ok(createdUser);
    }
}
