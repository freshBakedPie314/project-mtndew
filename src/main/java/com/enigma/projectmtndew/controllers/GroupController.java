package com.enigma.projectmtndew.controllers;

import com.enigma.projectmtndew.dtos.AddMembersRequestDTO;
import com.enigma.projectmtndew.dtos.GroupDTO;
import com.enigma.projectmtndew.dtos.GroupDetailedDTO;
import com.enigma.projectmtndew.services.GroupService;
import org.apache.catalina.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@AuthenticationPrincipal Jwt jwt,  @RequestBody GroupDTO groupDTO){
        UUID creatoriD = UUID.fromString(jwt.getSubject());

        GroupDTO group = groupService.createGroup(creatoriD, groupDTO);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupDTO> addMemebrs(@AuthenticationPrincipal Jwt jet, @PathVariable String groupId, @RequestBody AddMembersRequestDTO addMembersRequestDTO){
        UUID requesterId =  UUID.fromString(jet.getSubject());
        UUID groupUid = UUID.fromString(groupId);
        GroupDTO group = groupService.addMembers(requesterId, groupUid, addMembersRequestDTO);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailedDTO> getGroup(@AuthenticationPrincipal Jwt jwt, @PathVariable String groupId){
        UUID requesterId = UUID.fromString(jwt.getSubject());
        UUID groupUid = UUID.fromString(groupId);
        GroupDetailedDTO group = groupService.getGroup(requesterId, groupUid);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupDTO>> getUserGroups(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<GroupDTO> groups = groupService.findGroupByUserId(userId);
        return ResponseEntity.ok(groups);
    }
}
