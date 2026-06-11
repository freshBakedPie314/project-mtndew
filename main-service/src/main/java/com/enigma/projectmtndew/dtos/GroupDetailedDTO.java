package com.enigma.projectmtndew.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDetailedDTO {
    private UUID groupId;
    private String groupName;
    private String currency;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private String inviteCode;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GroupMembersDTO{
        private UUID userId;
        private String username;
        private String email;
    }

    private List<GroupMembersDTO> groupMembers;
}
