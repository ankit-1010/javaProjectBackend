package com.moneymate.service;

import com.moneymate.exception.BadRequestException;
import com.moneymate.exception.ForbiddenException;
import com.moneymate.exception.ResourceNotFoundException;
import com.moneymate.model.Budget;
import com.moneymate.repository.BudgetRepository;
import com.moneymate.repository.TransactionRepository;
import com.moneymate.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Budget> getCurrentUserBudgets() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Authentication required");
        }
        return getBudgetsByUserId(currentUserId);
    }

    public List<Budget> getBudgetsByUserId(Long requestedUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        // Enforce user data ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(requestedUserId))) {
            throw new ForbiddenException("Access denied: You cannot view another user's budgets");
        }

        List<Budget> budgets = budgetRepository.findByUserId(requestedUserId);

        // Derive spent amount from actual recorded transactions for each budget category
        for (Budget b : budgets) {
            BigDecimal actualSpent = transactionRepository.sumUserExpenseByCategory(b.getUserId(), b.getCategory());
            b.setSpentAmount(actualSpent != null ? actualSpent : BigDecimal.ZERO);
        }

        return budgets;
    }

    @Transactional
    public Budget createBudget(Budget budget) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        if (!isAdmin || budget.getUserId() == null) {
            if (currentUserId == null) {
                throw new BadRequestException("Authentication required to create a budget");
            }
            budget.setUserId(currentUserId);
        }

        if (budget.getAllocatedAmount() == null || budget.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Allocated budget amount must be strictly greater than 0");
        }

        if (budget.getCategory() == null || budget.getCategory().trim().isEmpty()) {
            throw new BadRequestException("Category is required for a budget");
        }
        budget.setCategory(budget.getCategory().trim());

        // Derive current spent amount from existing transactions for this user & category
        BigDecimal actualSpent = transactionRepository.sumUserExpenseByCategory(budget.getUserId(), budget.getCategory());
        budget.setSpentAmount(actualSpent != null ? actualSpent : BigDecimal.ZERO);

        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(Long id, Budget updated) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        // Enforce ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(existing.getUserId()))) {
            throw new ForbiddenException("Access denied: You cannot modify this budget");
        }

        if (updated.getAllocatedAmount() != null) {
            if (updated.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Allocated budget amount must be strictly greater than 0");
            }
            existing.setAllocatedAmount(updated.getAllocatedAmount());
        }

        if (updated.getCategory() != null && !updated.getCategory().trim().isEmpty()) {
            existing.setCategory(updated.getCategory().trim());
        }

        if (updated.getMonthYear() != null && !updated.getMonthYear().trim().isEmpty()) {
            existing.setMonthYear(updated.getMonthYear().trim());
        }

        if (updated.getIcon() != null) existing.setIcon(updated.getIcon());
        if (updated.getColor() != null) existing.setColor(updated.getColor());

        // Derive spent amount dynamically
        BigDecimal actualSpent = transactionRepository.sumUserExpenseByCategory(existing.getUserId(), existing.getCategory());
        existing.setSpentAmount(actualSpent != null ? actualSpent : BigDecimal.ZERO);

        return budgetRepository.save(existing);
    }

    @Transactional
    public void deleteBudget(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        // Enforce ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(existing.getUserId()))) {
            throw new ForbiddenException("Access denied: You cannot delete this budget");
        }

        budgetRepository.delete(existing);
    }
}
