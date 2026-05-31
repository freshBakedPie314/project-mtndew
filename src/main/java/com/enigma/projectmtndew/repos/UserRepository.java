package com.enigma.projectmtndew.repos;

import com.enigma.projectmtndew.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> findById(UUID id);

    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT gm.id.userId FROM GroupMember gm WHERE gm.id.groupId = :groupId)")
    List<User> findUsersByGroupId(@Param("groupId") UUID groupId);
}
