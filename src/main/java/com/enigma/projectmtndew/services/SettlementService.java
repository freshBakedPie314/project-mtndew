package com.enigma.projectmtndew.services;

import com.enigma.projectmtndew.dtos.SettlementDTO;
import com.enigma.projectmtndew.dtos.SettlementRequestDTO;
import com.enigma.projectmtndew.entities.NetBalance;
import com.enigma.projectmtndew.entities.NetBalanceId;
import com.enigma.projectmtndew.entities.Settlement;
import com.enigma.projectmtndew.repos.NetBalanceRepository;
import com.enigma.projectmtndew.repos.SettlementRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class SettlementService {
    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private NetBalanceRepository netBalanceRepository;

    @Autowired
    private MinimisationService minimisationService;

    public SettlementDTO settle(NetBalanceId netBalanceId, float amount) {
        NetBalance current = netBalanceRepository.findById(netBalanceId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No debt found for provided net balance id")
        );

        if(amount <= 0)
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Amount must be greater than zero");
        }

        if(amount > current.getAmount() + 0.001f)
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Settlement amount " + amount +
                    " exceeds current debt of " + current.getAmount());
        }

        Settlement settlement = new Settlement();
        settlement.setGroupId(netBalanceId.getGroupId());
        settlement.setPaidBy(netBalanceId.getFromUser());
        settlement.setPaidTo(netBalanceId.getToUser());
        settlement.setAmount(amount);
        Settlement saved = settlementRepository.save(settlement);

        float remaining = current.getAmount() - amount;

        if (remaining < 0.001f) {
            netBalanceRepository.delete(current);
        } else {
            current.setAmount(remaining);
            netBalanceRepository.save(current);
        }

        minimisationService.minimise(netBalanceId.getGroupId());

        return toDTO(saved);
    }

    private SettlementDTO toDTO(Settlement settlement) {
        SettlementDTO dto = new SettlementDTO();
        dto.setId(settlement.getId());
        dto.setGroupId(settlement.getGroupId());
        dto.setPaidBy(settlement.getPaidBy());
        dto.setPaidTo(settlement.getPaidTo());
        dto.setAmount(settlement.getAmount());
        dto.setSettledAt(settlement.getSettledAt());
        return dto;
    }
}
