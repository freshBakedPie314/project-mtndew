package com.enigma.projectmtndew.repos;

import com.enigma.projectmtndew.entities.Expense;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends CrudRepository<Expense, UUID> {
    List<Expense> getAllByGroupId(UUID groupId);
}
