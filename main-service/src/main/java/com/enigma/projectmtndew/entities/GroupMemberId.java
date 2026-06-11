package com.enigma.projectmtndew.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupMemberId implements Serializable {

    @Column(name = "group_id", nullable = false)
    private UUID groupId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
