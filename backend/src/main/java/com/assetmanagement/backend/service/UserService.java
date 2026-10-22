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
        User user = getUserEntityById(id);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmployeeCode(String employeeCode) {
        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with employee code: " + employeeCode));
        return toResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmployeeCode(request.employeeCode())) {
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
        
        String rawPassword = (request.password() != null && !request.password().isBlank())
                ? request.password()
                : "Password@123";
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        user.setDesignation(request.designation());
        user.setDateOfJoining(request.dateOfJoining());
        user.setAccountStatus("ACTIVE");
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserRequest request) {
        User user = getUserEntityById(userId);

        if (!user.getEmployeeCode().equalsIgnoreCase(request.employeeCode())
                && userRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Employee code already exists");
        }

        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists");
        }

        user.setEmployeeCode(request.employeeCode());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setDesignation(request.designation());
        user.setDateOfJoining(request.dateOfJoining());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserEntityById(userId);
        userRepository.delete(user);
    }

    public UserResponse toResponse(User user) {
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