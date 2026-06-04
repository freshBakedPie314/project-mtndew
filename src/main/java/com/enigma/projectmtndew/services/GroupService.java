package com.enigma.projectmtndew.services;

import com.enigma.projectmtndew.dtos.AddMembersRequestDTO;
import com.enigma.projectmtndew.dtos.GroupDTO;
import com.enigma.projectmtndew.dtos.GroupDetailedDTO;
import com.enigma.projectmtndew.dtos.UserDTO;
import com.enigma.projectmtndew.entities.Group;
import com.enigma.projectmtndew.entities.GroupMember;
import com.enigma.projectmtndew.entities.GroupMemberId;
import com.enigma.projectmtndew.repos.GroupMemebrRepository;
import com.enigma.projectmtndew.repos.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GroupService {
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    GroupMemebrRepository groupMemebrRepository;
    @Autowired
    UserService userService;

    public List<GroupDTO> findAllByIds(List<UUID> ids) {
        return  groupRepository.findAllByIdIn(ids).stream()
                .map(this::toGroupDTO)
                .toList();
}

    public GroupDTO toGroupDTO(Group group)
    {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(group.getId());
        groupDTO.setGroupName(group.getName());
        groupDTO.setCurrency(group.getCurrency());
        groupDTO.setCreatedBy(group.getCreatedBy());
        groupDTO.setCreatedAt(group.getCreatedAt());
        groupDTO.setInviteCode(group.getInviteCode());
        return groupDTO;
    }

    public GroupDTO addMembers(UUID requesterId, UUID groupId, AddMembersRequestDTO addMembersRequestDTO) {

        if(!groupMemebrRepository.existsByIdGroupIdAndIdUserId(groupId, requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not part of group");
        }

        Group group = groupRepository.findById(groupId).orElse(null);
        List<GroupMember> groupMember = addMembersRequestDTO.getUserId().stream()
                .filter(userId -> !groupMemebrRepository.existsById(new GroupMemberId(groupId, userId)))
                .map(userId -> GroupMember.builder()
                        .id(new GroupMemberId(groupId, userId))
                        .build())
                .toList();
        groupMemebrRepository.saveAll(groupMember);

        return toGroupDTO(group);
    }

    public GroupDTO joinGroup(UUID requesterId, String inviteCode) {
        UUID groupId = groupRepository.findByInviteCode(inviteCode);
        if(groupMemebrRepository.existsByIdGroupIdAndIdUserId(groupId, requesterId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already part of group");
        }
        GroupMember newMember = GroupMember.builder()
                                .id(new GroupMemberId(groupId, requesterId))
                                .build();
        groupMemebrRepository.save(newMember);
        Group group = groupRepository.findById(groupId).orElse(null);
        return toGroupDTO(group);
    }

    public GroupDetailedDTO getGroup(UUID requesterId, UUID groupId) {

        if(!groupMemebrRepository.existsById(new GroupMemberId(groupId, requesterId)))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not part of group");
        }

        Group group = groupRepository.findById(groupId).orElse(null);
        GroupDetailedDTO groupDetailedDTO = new GroupDetailedDTO();
        groupDetailedDTO.setGroupName(group.getName());
        groupDetailedDTO.setCurrency(group.getCurrency());
        groupDetailedDTO.setInviteCode(group.getInviteCode());
        groupDetailedDTO.setCreatedAt(group.getCreatedAt());
        groupDetailedDTO.setCreatedBy(group.getCreatedBy());

        groupDetailedDTO.setGroupMembers(
                userService.getUsersInAGroup(groupId).stream()
                        .map(gm -> {
                            GroupDetailedDTO.GroupMembersDTO memeber = new GroupDetailedDTO.GroupMembersDTO();
                            memeber.setUsername(gm.getUsername());
                            memeber.setEmail(gm.getEmail());
                            memeber.setUserId(gm.getId());
                            return memeber;
                        })
                        .toList()
        );

        return groupDetailedDTO;
    }

    public List<GroupDTO> findGroupByUserId(UUID id) {
        List<UUID> groupIds = groupMemebrRepository.findByIdUserId(id)
                .stream()
                .map(groupMember -> groupMember.getId().getGroupId())
                .toList();

        return findAllByIds(groupIds);
    }

    //============HELPERS==============

    public GroupDTO createGroup(UUID creatorId, GroupDTO groupDTO) {
        Group group = new Group();
        group.setName(groupDTO.getGroupName());
        group.setCurrency(groupDTO.getCurrency());
        group.setCreatedBy(creatorId);

        Group savedGroup = groupRepository.save(group);
        return toGroupDTO(group);
    }

}
