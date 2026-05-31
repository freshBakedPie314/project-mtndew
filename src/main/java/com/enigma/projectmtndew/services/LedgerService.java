package com.enigma.projectmtndew.services;

import com.enigma.projectmtndew.dtos.ExpenseDTO;
import com.enigma.projectmtndew.entities.Expense;
import com.enigma.projectmtndew.entities.ExpenseSplit;
import com.enigma.projectmtndew.repos.ExpenseRepository;
import com.enigma.projectmtndew.repos.ExpenseSplitRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MinimisationService minimisationService;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Transactional
    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        Expense expense = fromDTO(expenseDTO);
        Expense savedExpense = expenseRepository.save(expense);

        float sharedAmount = savedExpense.getAmount() / expenseDTO.getSplits().size();

        List<ExpenseSplit> expenseSplits = expenseDTO.getSplits().stream()
                .map(split -> {
                    ExpenseSplit expenseSplit = new ExpenseSplit();
                    expenseSplit.setUserId(split.getId());
                    expenseSplit.setShareAmount(sharedAmount);
                    expenseSplit.setExpense(savedExpense);
                    return expenseSplit;
                })
                .toList();

        expenseSplitRepository.saveAll(expenseSplits);
        ExpenseDTO savedExpenseDTO = toDTO(savedExpense, expenseSplits);
        minimisationService.expenseAdded(savedExpenseDTO);
        return savedExpenseDTO;
    }

    Expense fromDTO(ExpenseDTO expenseDTO) {
        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDescription(expenseDTO.getDescription());
        expense.setGroupId(expenseDTO.getGroupId());
        expense.setPaidBy(expenseDTO.getPaidBy());
        return expense;
    }

    ExpenseDTO toDTO(Expense expense, List<ExpenseSplit> splits) {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setId(expense.getId());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setDescription(expense.getDescription());
        expenseDTO.setGroupId(expense.getGroupId());
        expenseDTO.setPaidBy(expense.getPaidBy());
        expenseDTO.setAddedAt(expense.getAddedAt());

        expenseDTO.setSplits(splits.stream()
                .map(expenseSplit -> {
                    ExpenseDTO.ExpenseSplitDTO expenseSplitDTO = new ExpenseDTO.ExpenseSplitDTO();
                    expenseSplitDTO.setId(expenseSplit.getUserId());
                    expenseSplitDTO.setAmount(expenseSplit.getShareAmount());
                    return expenseSplitDTO;
                }).toList());
        return expenseDTO;
    }

    ExpenseDTO toDTORaw(Expense expense) {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setId(expense.getId());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setDescription(expense.getDescription());
        expenseDTO.setGroupId(expense.getGroupId());
        expenseDTO.setPaidBy(expense.getPaidBy());
        expenseDTO.setAddedAt(expense.getAddedAt());

        expenseDTO.setSplits(expense.getExpenseSplits().stream()
                .map(expenseSplit -> {
                    ExpenseDTO.ExpenseSplitDTO expenseSplitDTO = new ExpenseDTO.ExpenseSplitDTO();
                    expenseSplitDTO.setId(expenseSplit.getId());
                    expenseSplitDTO.setAmount(expenseSplit.getShareAmount());
                    return expenseSplitDTO;
                }).toList());
        return expenseDTO;
    }

    public List<ExpenseDTO> getExpenseByGroupId(UUID groupId) {
        return expenseRepository.getAllByGroupId(groupId).stream()
                .map(this::toDTORaw)
                .toList();
    }
}
