package com.moneymate.service;

import com.moneymate.dto.*;
import com.moneymate.exception.BadRequestException;
import com.moneymate.exception.DuplicateResourceException;
import com.moneymate.exception.ForbiddenException;
import com.moneymate.exception.ResourceNotFoundException;
import com.moneymate.model.User;
import com.moneymate.repository.TransactionRepository;
import com.moneymate.repository.UserRepository;
import com.moneymate.security.JwtUtils;
import com.moneymate.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            throw new ForbiddenException("Your account is currently inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(true, "Login successful", token, user);
    }

    public AuthResponse adminLogin(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid admin credentials"));

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ForbiddenException("Access denied: Administrator privileges required");
        }

        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            throw new ForbiddenException("Admin account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid admin credentials");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(true, "Admin authentication successful", token, user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateResourceException("An account with email " + cleanEmail + " already exists");
        }

        String initial = request.getFullName() != null && !request.getFullName().trim().isEmpty()
                ? request.getFullName().trim().substring(0, 1).toUpperCase() : "U";

        User newUser = new User(
                request.getFullName().trim(),
                cleanEmail,
                passwordEncoder.encode(request.getPassword()),
                "USER",
                "Active",
                request.getPhone(),
                initial,
                LocalDate.now(),
                BigDecimal.ZERO
        );

        User saved = userRepository.save(newUser);
        String token = jwtUtils.generateToken(saved.getId(), saved.getEmail(), saved.getRole());
        return new AuthResponse(true, "Account created successfully", token, saved);
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findByOrderByJoinedOnDesc();
        for (User u : users) {
            u.setTxCount(transactionRepository.countByUserId(u.getId()));
        }
        return users;
    }

    public Optional<User> getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresent(u -> u.setTxCount(transactionRepository.countByUserId(u.getId())));
        return userOpt;
    }

    @Transactional
    public User createUser(User user) {
        String cleanEmail = user.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateResourceException("Email already in use: " + cleanEmail);
        }

        user.setEmail(cleanEmail);
        String rawPassword = user.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "password123";
        }
        user.setPassword(passwordEncoder.encode(rawPassword));

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("USER");
        }
        if (user.getStatus() == null || user.getStatus().trim().isEmpty()) {
            user.setStatus("Active");
        }
        if (user.getTotalBalance() == null) {
            user.setTotalBalance(BigDecimal.ZERO);
        }
        if (user.getJoinedOn() == null) {
            user.setJoinedOn(LocalDate.now());
        }

        User saved = userRepository.save(user);
        saved.setTxCount(0L);
        return saved;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (updatedUser.getFullName() != null && !updatedUser.getFullName().trim().isEmpty()) {
            user.setFullName(updatedUser.getFullName().trim());
            user.setAvatar(user.getFullName().substring(0, 1).toUpperCase());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().trim().isEmpty()) {
            String newEmail = updatedUser.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("Email is already taken by another user");
            }
            user.setEmail(newEmail);
        }
        if (updatedUser.getPhone() != null) {
            user.setPhone(updatedUser.getPhone().trim());
        }
        if (updatedUser.getStatus() != null && !updatedUser.getStatus().trim().isEmpty()) {
            user.setStatus(updatedUser.getStatus().trim());
        }
        if (updatedUser.getRole() != null && !updatedUser.getRole().trim().isEmpty()) {
            user.setRole(updatedUser.getRole().trim().toUpperCase());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword().trim()));
        }

        User saved = userRepository.save(user);
        saved.setTxCount(transactionRepository.countByUserId(saved.getId()));
        return saved;
    }

    @Transactional
    public User toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String newStatus = "Active".equalsIgnoreCase(user.getStatus()) ? "Inactive" : "Active";
        user.setStatus(newStatus);
        User saved = userRepository.save(user);
        saved.setTxCount(transactionRepository.countByUserId(saved.getId()));
        return saved;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User getCurrentUserProfile() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("No authenticated user session found");
        }
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        user.setTxCount(transactionRepository.countByUserId(user.getId()));
        return user;
    }

    @Transactional
    public User updateCurrentUserProfile(UpdateProfileRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("No authenticated user session found");
        }
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
            user.setAvatar(user.getFullName().substring(0, 1).toUpperCase());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar().trim());
        }

        User saved = userRepository.save(user);
        saved.setTxCount(transactionRepository.countByUserId(saved.getId()));
        return saved;
    }

    @Transactional
    public void changeCurrentUserPassword(ChangePasswordRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("No authenticated user session found");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(user);
    }

    @Transactional
    public void changeAdminPassword(ChangePasswordRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("No authenticated admin session found");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User admin = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin account not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(admin);
    }
}
