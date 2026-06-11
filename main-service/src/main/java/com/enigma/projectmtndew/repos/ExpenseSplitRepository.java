package com.enigma.projectmtndew.repos;

import com.enigma.projectmtndew.entities.ExpenseSplit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends CrudRepository<ExpenseSplit, UUID> {
}
