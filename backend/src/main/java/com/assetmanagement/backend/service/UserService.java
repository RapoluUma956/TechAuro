package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.UserRequest;
import com.assetmanagement.backend.dto.UserResponse;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return toResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmployeeCode(
                request.employeeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee code already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists");
        }

        User user = new User();

        user.setEmployeeCode(request.employeeCode());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(
                passwordEncoder.encode(request.password()));
        user.setDesignation(request.designation());
        user.setDateOfJoining(request.dateOfJoining());
        user.setAccountStatus("ACTIVE");
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmployeeCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getDesignation(),
                user.getDateOfJoining(),
                user.getAccountStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
