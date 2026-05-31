package com.enigma.projectmtndew.controllers;

import com.enigma.projectmtndew.dtos.GroupDTO;
import com.enigma.projectmtndew.dtos.UserDTO;
import com.enigma.projectmtndew.services.GroupService;
import com.enigma.projectmtndew.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserDTO user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }
}
