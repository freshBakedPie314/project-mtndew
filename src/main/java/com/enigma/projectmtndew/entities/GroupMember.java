package com.enigma.projectmtndew.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="group_members")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    private void prePersist()
    {
        joinedAt = LocalDateTime.now();
    }
}
