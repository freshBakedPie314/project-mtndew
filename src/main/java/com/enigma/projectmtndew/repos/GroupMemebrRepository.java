package com.enigma.projectmtndew.repos;

import com.enigma.projectmtndew.entities.GroupMember;
import com.enigma.projectmtndew.entities.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMemebrRepository extends JpaRepository<GroupMember, GroupMemberId> {
    List<GroupMember> findByIdGroupId(UUID groupId);
    List<GroupMember> findByIdUserId(UUID userId);
    boolean existsByIdGroupIdAndIdUserId(UUID groupId, UUID userId);

    @Query("SELECT gm.id.userId FROM GroupMember gm WHERE gm.id.groupId = :groupId")
    List<UUID> findUserIdsByGroupId(@Param("groupId") UUID groupId);
}
