package com.enigma.projectmtndew.dtos;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private UUID id;
    private String groupName;
    private String currency;
    private UUID createdBy;
    private String inviteCode;
    private LocalDateTime createdAt;
}
