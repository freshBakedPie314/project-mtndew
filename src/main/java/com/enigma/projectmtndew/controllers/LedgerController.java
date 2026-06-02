package com.enigma.projectmtndew.controllers;

import com.enigma.projectmtndew.dtos.ExpenseDTO;
import com.enigma.projectmtndew.dtos.ScannedReceiptExpenseRequestDTO;
import com.enigma.projectmtndew.dtos.SettlementDTO;
import com.enigma.projectmtndew.dtos.SettlementRequestDTO;
import com.enigma.projectmtndew.entities.NetBalanceId;
import com.enigma.projectmtndew.entities.SimplifiedDebts;
import com.enigma.projectmtndew.services.LedgerService;
import com.enigma.projectmtndew.services.OcrService;
import com.enigma.projectmtndew.services.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ledger")
public class LedgerController {

    @Autowired
    LedgerService ledgerService;

    @Autowired
    OcrService ocrService;

    @Autowired
    SettlementService settlementService;

    @PostMapping("/add")
    public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO expense = ledgerService.addExpense(expenseDTO);
        return ResponseEntity.ok(expense);
    }

    @PostMapping("/add/scanned")
    public ResponseEntity<ExpenseDTO> addScannedExpense(@AuthenticationPrincipal Jwt jwt, @RequestBody ScannedReceiptExpenseRequestDTO request) {
        request.setPaidBy(UUID.fromString(jwt.getSubject()));
        ExpenseDTO expenseDTO = ocrService.handleScannedReceiptExpenseRequest(request);
        return ResponseEntity.ok(ledgerService.addExpense(expenseDTO));
    }


    @GetMapping("/raw/{groupId}")
    public ResponseEntity<List<ExpenseDTO>> getExpense(@PathVariable("groupId") String groupId) {
        UUID uuid = UUID.fromString(groupId);

        List<ExpenseDTO> exoenses = ledgerService.getExpenseByGroupId(uuid);
        return ResponseEntity.ok(exoenses);
    }

    @PostMapping("/settle")
    public ResponseEntity<SettlementDTO> settle(@AuthenticationPrincipal Jwt jwt, @RequestBody SettlementRequestDTO request) {

        UUID uuid = UUID.fromString(jwt.getSubject());
        if(!uuid.equals(request.getFromUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only user can pay his setllement");
        }

        NetBalanceId netBalanceId = new NetBalanceId();
        netBalanceId.setGroupId(request.getGroupId());
        netBalanceId.setFromUser(request.getFromUser());
        netBalanceId.setToUser(request.getToUser());

        SettlementDTO result = settlementService.settle(netBalanceId, request.getAmount());
        return ResponseEntity.ok(result);
    }
}
