package com.moneymate.service;

import com.moneymate.exception.BadRequestException;
import com.moneymate.exception.ForbiddenException;
import com.moneymate.exception.ResourceNotFoundException;
import com.moneymate.model.Category;
import com.moneymate.model.Transaction;
import com.moneymate.model.User;
import com.moneymate.repository.CategoryRepository;
import com.moneymate.repository.TransactionRepository;
import com.moneymate.repository.UserRepository;
import com.moneymate.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findByOrderByTransactionDateDesc();
    }

    public List<Transaction> getTransactionsForCurrentUser(String type) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("No authenticated user found");
        }
        return getTransactionsByUserIdAndType(currentUserId, type);
    }

    public List<Transaction> getTransactionsByUserIdAndType(Long requestedUserId, String type) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        // Enforce user data ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(requestedUserId))) {
            throw new ForbiddenException("Access denied: You cannot view another user's transactions");
        }

        if ("All".equalsIgnoreCase(type) || type == null || type.trim().isEmpty()) {
            return transactionRepository.findByUserIdOrderByTransactionDateDesc(requestedUserId);
        }
        return transactionRepository.findByUserIdAndTypeOrderByTransactionDateDesc(requestedUserId, type.trim());
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        // Enforce ownership: normal users can only create transactions for themselves
        if (!isAdmin || transaction.getUserId() == null) {
            if (currentUserId == null) {
                throw new BadRequestException("Authentication required to create transactions");
            }
            transaction.setUserId(currentUserId);
        }

        // Validate amount
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be strictly greater than 0");
        }

        if (transaction.getType() == null || (!"Income".equalsIgnoreCase(transaction.getType()) && !"Expense".equalsIgnoreCase(transaction.getType()))) {
            throw new BadRequestException("Transaction type must be 'Income' or 'Expense'");
        }

        // Standardize type
        String normalizedType = "Income".equalsIgnoreCase(transaction.getType()) ? "Income" : "Expense";
        transaction.setType(normalizedType);

        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }

        // Fetch User and update balance
        User user = userRepository.findById(transaction.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for transaction"));

        transaction.setUserName(user.getFullName());

        BigDecimal currentBalance = user.getTotalBalance() != null ? user.getTotalBalance() : BigDecimal.ZERO;
        if ("Income".equalsIgnoreCase(normalizedType)) {
            user.setTotalBalance(currentBalance.add(transaction.getAmount()));
        } else {
            user.setTotalBalance(currentBalance.subtract(transaction.getAmount()));
        }
        userRepository.save(user);

        // Update category transaction count if applicable
        if (transaction.getCategory() != null) {
            categoryRepository.findByNameIgnoreCase(transaction.getCategory().trim()).ifPresent(cat -> {
                int count = cat.getTransactionCount() != null ? cat.getTransactionCount() : 0;
                cat.setTransactionCount(count + 1);
                categoryRepository.save(cat);
            });
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long id, Transaction updated) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Enforce ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(existing.getUserId()))) {
            throw new ForbiddenException("Access denied: You cannot modify this transaction");
        }

        if (updated.getAmount() != null && updated.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be strictly greater than 0");
        }

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for transaction"));

        BigDecimal balance = user.getTotalBalance() != null ? user.getTotalBalance() : BigDecimal.ZERO;

        // 1. Revert existing transaction's effect on user balance
        if ("Income".equalsIgnoreCase(existing.getType())) {
            balance = balance.subtract(existing.getAmount());
        } else if ("Expense".equalsIgnoreCase(existing.getType())) {
            balance = balance.add(existing.getAmount());
        }

        // Update fields
        if (updated.getDescription() != null && !updated.getDescription().trim().isEmpty()) {
            existing.setDescription(updated.getDescription().trim());
        }

        String oldCategory = existing.getCategory();
        if (updated.getCategory() != null && !updated.getCategory().trim().isEmpty()) {
            existing.setCategory(updated.getCategory().trim());
        }

        if (updated.getType() != null && !updated.getType().trim().isEmpty()) {
            String newType = "Income".equalsIgnoreCase(updated.getType()) ? "Income" : "Expense";
            existing.setType(newType);
        }

        if (updated.getAmount() != null) {
            existing.setAmount(updated.getAmount());
        }

        if (updated.getTransactionDate() != null) {
            existing.setTransactionDate(updated.getTransactionDate());
        }

        if (updated.getStatus() != null && !updated.getStatus().trim().isEmpty()) {
            existing.setStatus(updated.getStatus().trim());
        }

        // 2. Apply new transaction's effect on user balance
        if ("Income".equalsIgnoreCase(existing.getType())) {
            balance = balance.add(existing.getAmount());
        } else if ("Expense".equalsIgnoreCase(existing.getType())) {
            balance = balance.subtract(existing.getAmount());
        }

        user.setTotalBalance(balance);
        userRepository.save(user);

        // Update Category counts if category changed
        if (oldCategory != null && !oldCategory.equalsIgnoreCase(existing.getCategory())) {
            categoryRepository.findByNameIgnoreCase(oldCategory).ifPresent(oldCat -> {
                int count = oldCat.getTransactionCount() != null ? oldCat.getTransactionCount() : 0;
                oldCat.setTransactionCount(Math.max(0, count - 1));
                categoryRepository.save(oldCat);
            });
            categoryRepository.findByNameIgnoreCase(existing.getCategory()).ifPresent(newCat -> {
                int count = newCat.getTransactionCount() != null ? newCat.getTransactionCount() : 0;
                newCat.setTransactionCount(count + 1);
                categoryRepository.save(newCat);
            });
        }

        return transactionRepository.save(existing);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Enforce ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(existing.getUserId()))) {
            throw new ForbiddenException("Access denied: You cannot delete this transaction");
        }

        // Revert effect on balance
        User user = userRepository.findById(existing.getUserId()).orElse(null);
        if (user != null) {
            BigDecimal balance = user.getTotalBalance() != null ? user.getTotalBalance() : BigDecimal.ZERO;
            if ("Income".equalsIgnoreCase(existing.getType())) {
                balance = balance.subtract(existing.getAmount());
            } else if ("Expense".equalsIgnoreCase(existing.getType())) {
                balance = balance.add(existing.getAmount());
            }
            user.setTotalBalance(balance);
            userRepository.save(user);
        }

        // Decrement category transaction count
        if (existing.getCategory() != null) {
            categoryRepository.findByNameIgnoreCase(existing.getCategory()).ifPresent(cat -> {
                int count = cat.getTransactionCount() != null ? cat.getTransactionCount() : 0;
                cat.setTransactionCount(Math.max(0, count - 1));
                categoryRepository.save(cat);
            });
        }

        transactionRepository.delete(existing);
    }
}
