package com.enigma.projectmtndew.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NetBalanceId {
    private UUID groupId;
    private UUID fromUser;
    private UUID toUser;
}
