package com.enigma.projectmtndew.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "net_balances")
@Data
public class NetBalance {

    @EmbeddedId
    private NetBalanceId id;

    private float amount;
}
