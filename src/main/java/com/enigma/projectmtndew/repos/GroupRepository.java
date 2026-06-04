package com.enigma.projectmtndew.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enigma.projectmtndew.entities.Group;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findById(UUID id);
    List<Group> findAllByIdIn(List<UUID> ids);

    UUID findByInviteCode(String inviteCode);
}
