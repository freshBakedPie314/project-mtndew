package com.enigma.projectmtndew.services;

import com.enigma.projectmtndew.dtos.ExpenseDTO;
import com.enigma.projectmtndew.entities.Expense;
import com.enigma.projectmtndew.entities.ExpenseSplit;
import com.enigma.projectmtndew.repos.ExpenseRepository;
import com.enigma.projectmtndew.repos.ExpenseSplitRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        return switch (expenseDTO.getSplitType()) {
            case EQUAL      -> handleEqualSplit(expenseDTO);
            case PERCENTAGE -> handlePercentageSplit(expenseDTO);
            case OCR        -> handleEqualSplit(expenseDTO); // OCR goes through percentage, not here
        };
    }

    // =========== EQUAL SPLIT ==================

    private ExpenseDTO handleEqualSplit(ExpenseDTO expenseDTO) {
        Expense expense = fromDTO(expenseDTO);
        Expense saved   = expenseRepository.save(expense);

        float sharedAmount = saved.getAmount() / expenseDTO.getSplits().size();

        List<ExpenseSplit> splits = expenseDTO.getSplits().stream()
                .map(split -> {
                    ExpenseSplit es = new ExpenseSplit();
                    es.setUserId(split.getId());
                    es.setShareAmount(sharedAmount);
                    es.setExpense(saved);
                    return es;
                })
                .toList();

        expenseSplitRepository.saveAll(splits);
        ExpenseDTO savedDTO = toDTO(saved, splits);
        minimisationService.expenseAdded(savedDTO);
        return savedDTO;
    }

    // =========== PERCENT SPLIT ==================

    private ExpenseDTO handlePercentageSplit(ExpenseDTO expenseDTO) {

        // validate percentages sum to 100
        float totalPercent = expenseDTO.getSplits().stream()
                .map(ExpenseDTO.ExpenseSplitDTO::getPercent)
                .reduce(0f, Float::sum);

        if (Math.abs(totalPercent - 100f) > 0.01f) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Percentages must sum to 100, got: " + totalPercent);
        }

        Expense expense = fromDTO(expenseDTO);
        Expense saved   = expenseRepository.save(expense);

        List<ExpenseSplit> splits = expenseDTO.getSplits().stream()
                .map(split -> {
                    float amount = (split.getPercent() / 100f) * saved.getAmount();
                    ExpenseSplit es = new ExpenseSplit();
                    es.setUserId(split.getId());
                    es.setShareAmount(amount);
                    es.setExpense(saved);
                    return es;
                })
                .toList();

        expenseSplitRepository.saveAll(splits);
        ExpenseDTO savedDTO = toDTO(saved, splits);
        minimisationService.expenseAdded(savedDTO);
        return savedDTO;
    }

    // =========== HELPERS  ==================

    Expense fromDTO(ExpenseDTO expenseDTO) {
        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDescription(expenseDTO.getDescription());
        expense.setGroupId(expenseDTO.getGroupId());
        expense.setPaidBy(expenseDTO.getPaidBy());
        return expense;
    }

    ExpenseDTO toDTO(Expense expense, List<ExpenseSplit> splits) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());
        dto.setGroupId(expense.getGroupId());
        dto.setPaidBy(expense.getPaidBy());
        dto.setAddedAt(expense.getAddedAt());
        dto.setSplits(splits.stream()
                .map(es -> {
                    ExpenseDTO.ExpenseSplitDTO splitDTO = new ExpenseDTO.ExpenseSplitDTO();
                    splitDTO.setId(es.getUserId());
                    splitDTO.setAmount(es.getShareAmount());
                    return splitDTO;
                }).toList());
        return dto;
    }

    ExpenseDTO toDTORaw(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());
        dto.setGroupId(expense.getGroupId());
        dto.setPaidBy(expense.getPaidBy());
        dto.setAddedAt(expense.getAddedAt());
        dto.setSplits(expense.getExpenseSplits().stream()
                .map(es -> {
                    ExpenseDTO.ExpenseSplitDTO splitDTO = new ExpenseDTO.ExpenseSplitDTO();
                    splitDTO.setId(es.getUserId());
                    splitDTO.setAmount(es.getShareAmount());
                    return splitDTO;
                }).toList());
        return dto;
    }

    public List<ExpenseDTO> getExpenseByGroupId(UUID groupId) {
        return expenseRepository.getAllByGroupId(groupId).stream()
                .map(this::toDTORaw)
                .toList();
    }
}