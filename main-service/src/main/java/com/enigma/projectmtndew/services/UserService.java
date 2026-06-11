package com.enigma.projectmtndew.services;

import com.enigma.projectmtndew.dtos.GroupDTO;
import com.enigma.projectmtndew.dtos.UserDTO;
import com.enigma.projectmtndew.dtos.UserExpenseSummaryResponseDTO;
import com.enigma.projectmtndew.entities.Group;
import com.enigma.projectmtndew.entities.GroupMember;
import com.enigma.projectmtndew.entities.NetBalance;
import com.enigma.projectmtndew.entities.User;
import com.enigma.projectmtndew.repos.GroupMemebrRepository;
import com.enigma.projectmtndew.repos.GroupRepository;
import com.enigma.projectmtndew.repos.NetBalanceRepository;
import com.enigma.projectmtndew.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemebrRepository groupMemebrRepository;

    @Autowired
    private NetBalanceRepository netBalanceRepository;


    public UserDTO syncUser(UUID id, String email, String username) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setEmail(email);
                    existingUser.setUsername(username);
                    return toDTO(userRepository.save(existingUser));
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(id);
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    return toDTO(userRepository.save(newUser));
                });
    }

    public UserDTO getUser(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        //TODO: Handle null
        return toDTO(user);
    }

    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        User user = userRepository.findById(id).orElse(null);
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setId(userDTO.getId());
        userRepository.save(user);
        return toDTO(user);
    }

    public List<UserDTO> getUsersInAGroup(UUID groupId) {
        List<User> users = userRepository.findUsersByGroupId(groupId);
        return users.stream().map( user ->
                toDTO(user))
                .toList();
    }

    public UserExpenseSummaryResponseDTO getUserExpenseSummary(UUID id) {

        //get all owed -> fromUserId = id
        float amountOwed = 0;
        List<NetBalance> netBalancesOwed = netBalanceRepository.findAllByIdFromUser(id);
        for (NetBalance netBalance : netBalancesOwed) {
            amountOwed += netBalance.getAmount();
        }

        //get all lent -> toUserId = id
        float amountLent = 0;
        List<NetBalance> netBalancesLent = netBalanceRepository.findAllByIdToUser(id);
        for (NetBalance netBalance : netBalancesLent) {
            amountLent += netBalance.getAmount();
        }

        UserExpenseSummaryResponseDTO responseDTO = new UserExpenseSummaryResponseDTO();
        responseDTO.setTotalLent(amountLent);
        responseDTO.setTotalOwed(amountOwed);
        return responseDTO;
    }


    //=======HELPERS========


    public User fromDTO(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        return user;
    }

    public UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }
}
