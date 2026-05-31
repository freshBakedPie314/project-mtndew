package com.enigma.projectmtndew.repos;

import com.enigma.projectmtndew.entities.SimplifiedDebts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SimplifiedDebtsRepository extends JpaRepository<SimplifiedDebts, UUID> {
    public void deleteSimplifiedDebtsByGroupId(UUID groupId);
}
