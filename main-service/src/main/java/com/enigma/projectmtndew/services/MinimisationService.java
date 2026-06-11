package com.enigma.projectmtndew.services;

import com.enigma.projectmtndew.dtos.ExpenseDTO;
import com.enigma.projectmtndew.entities.NetBalance;
import com.enigma.projectmtndew.entities.NetBalanceId;
import com.enigma.projectmtndew.entities.SimplifiedDebts;
import com.enigma.projectmtndew.repos.NetBalanceRepository;
import com.enigma.projectmtndew.repos.SimplifiedDebtsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class MinimisationService {

    @Autowired
    private NetBalanceRepository netBalanceRepository;

    @Autowired
    private SimplifiedDebtsRepository simplifiedDebtsRepository;

    @Transactional
    public List<SimplifiedDebts> expenseAdded(ExpenseDTO expenseDTO) {

        UUID groupId = expenseDTO.getGroupId();
        UUID toUser = expenseDTO.getPaidBy();

        Map<NetBalanceId, Float> currentBalances = netBalanceRepository.findByIdGroupId(groupId).stream()
                .collect(Collectors.toMap(NetBalance::getId, NetBalance::getAmount));

        List<NetBalance> newNetBalances = expenseDTO.getSplits().stream()
                .filter(split -> !split.getId().equals(toUser))
                .map(split -> {
                    NetBalanceId forwardId = new NetBalanceId();
                    forwardId.setGroupId(groupId);
                    forwardId.setFromUser(split.getId());
                    forwardId.setToUser(toUser);

                    NetBalanceId reverseId = new NetBalanceId();
                    reverseId.setGroupId(groupId);
                    reverseId.setFromUser(toUser);
                    reverseId.setToUser(split.getId());

                    float forwardExisting = currentBalances.getOrDefault(forwardId, 0f);
                    float reverseExisting = currentBalances.getOrDefault(reverseId, 0f);

                    if (forwardExisting > 0) {
                        // same direction row exists — just add to it
                        NetBalance nb = new NetBalance();
                        nb.setId(forwardId);
                        nb.setAmount(forwardExisting + split.getAmount());
                        return nb;

                    } else if (reverseExisting > 0) {
                        // opposite direction row exists, net them off
                        float net = reverseExisting - split.getAmount();

                        if (net > 0.001f) {
                            // reverse still dominates, reduce it
                            NetBalance nb = new NetBalance();
                            nb.setId(reverseId);
                            nb.setAmount(net);
                            return nb;

                        } else if (net < -0.001f) {
                            // forward now dominates, delete reverse, create forward
                            netBalanceRepository.deleteById(reverseId);
                            NetBalance nb = new NetBalance();
                            nb.setId(forwardId);
                            nb.setAmount(Math.abs(net));
                            return nb;

                        } else {
                            // exactly cancelled, delete reverse row
                            netBalanceRepository.deleteById(reverseId);
                            return null;
                        }

                    } else {
                        // no existing row either direction, create new
                        NetBalance nb = new NetBalance();
                        nb.setId(forwardId);
                        nb.setAmount(split.getAmount());
                        return nb;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        netBalanceRepository.saveAll(newNetBalances);

        return minimise(groupId);
    }

    public List<SimplifiedDebts> minimise(UUID groupId) {
        PriorityQueue<Map.Entry<UUID, Float>> creditors = new PriorityQueue<Map.Entry<UUID, Float>>(
                (a,b) -> Float.compare(b.getValue(), a.getValue())
        );

        PriorityQueue<Map.Entry<UUID, Float>> debitors = new PriorityQueue<Map.Entry<UUID, Float>>(
                (a,b) -> Float.compare(a.getValue(), b.getValue())
        );

        for( NetBalance nb : netBalanceRepository.findByIdGroupId(groupId) )
        {
            UUID from = nb.getId().getFromUser();
            UUID to = nb.getId().getToUser();
            float amount  = nb.getAmount();
            creditors.add(Map.entry(to, amount));
            debitors.add(Map.entry(from, -amount));
        }

        List<SimplifiedDebts> finalSimplifiedDebts = new ArrayList<>();
        while(!creditors.isEmpty() && !debitors.isEmpty()) {
            Map.Entry<UUID, Float> topCreditor = creditors.poll();
            Map.Entry<UUID, Float> topDebitor = debitors.poll();

            float settledAmount = Math.min(topCreditor.getValue(), -topDebitor.getValue());

            SimplifiedDebts simplifiedDebts = new SimplifiedDebts();
            simplifiedDebts.setGroupId(groupId);
            simplifiedDebts.setFromUserId(topDebitor.getKey());
            simplifiedDebts.setToUserId(topCreditor.getKey());
            simplifiedDebts.setAmount(settledAmount);
            simplifiedDebts.setSettled(false);

            finalSimplifiedDebts.add(simplifiedDebts);

            float creditorRemaining = topCreditor.getValue() - settledAmount;
            float debitorRemaining = topDebitor.getValue() + settledAmount;

            if(creditorRemaining > 0.001f)
            {
                creditors.add(Map.entry(topCreditor.getKey(), creditorRemaining));
            }
            if(debitorRemaining > 0.001f)
            {
                debitors.add(Map.entry(topDebitor.getKey(), debitorRemaining));
            }
        }

        //Update simplified_debt
        simplifiedDebtsRepository.deleteSimplifiedDebtsByGroupId(groupId);
        simplifiedDebtsRepository.saveAll(finalSimplifiedDebts);

        return finalSimplifiedDebts;
    }
}
