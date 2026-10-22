package org.example.assetmanagement.service;

import org.example.assetmanagement.entity.*;
import org.example.assetmanagement.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );
    }

    public User getUserByEmployeeCode(String employeeCode) {

        return userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with employee code: "
                                        + employeeCode
                        )
                );
    }

    @Transactional
    public User createUser(User user) {

        if (userRepository.existsByEmployeeCode(
                user.getEmployeeCode())) {

            throw new RuntimeException(
                    "Employee code already exists"
            );
        }

        if (userRepository.existsByEmail(user.getEmail())) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long userId, User updatedUser) {

        User existingUser = getUserById(userId);

        existingUser.setEmployeeCode(
                updatedUser.getEmployeeCode()
        );

        existingUser.setFirstName(
                updatedUser.getFirstName()
        );

        existingUser.setLastName(
                updatedUser.getLastName()
        );

        existingUser.setEmail(
                updatedUser.getEmail()
        );

        existingUser.setPhone(
                updatedUser.getPhone()
        );

        existingUser.setDesignation(
                updatedUser.getDesignation()
        );

        existingUser.setProfileImage(
                updatedUser.getProfileImage()
        );

        existingUser.setDateOfJoining(
                updatedUser.getDateOfJoining()
        );

        existingUser.setAccountStatus(
                updatedUser.getAccountStatus()
        );

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long userId) {

        User user = getUserById(userId);

        userRepository.delete(user);
    }
}