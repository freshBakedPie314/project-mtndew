package com.enigma.projectmtndew.repos;

import com.enigma.projectmtndew.dtos.SimplifiedDebtResponseDTO;
import com.enigma.projectmtndew.entities.NetBalance;
import com.enigma.projectmtndew.entities.NetBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NetBalanceRepository extends JpaRepository<NetBalance, NetBalanceId> {
    List<NetBalance> findByIdGroupId(UUID groupId);
    List<NetBalance> findAllByIdFromUser(UUID userId);
    List<NetBalance> findAllByIdToUser(UUID userId);
}
