package com.moneymate.service;

import com.moneymate.exception.BadRequestException;
import com.moneymate.exception.ForbiddenException;
import com.moneymate.exception.ResourceNotFoundException;
import com.moneymate.model.Goal;
import com.moneymate.repository.GoalRepository;
import com.moneymate.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    public List<Goal> getCurrentUserGoals() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Authentication required");
        }
        return getGoalsByUserId(currentUserId);
    }

    public List<Goal> getGoalsByUserId(Long requestedUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        // Enforce user data ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(requestedUserId))) {
            throw new ForbiddenException("Access denied: You cannot view another user's goals");
        }

        return goalRepository.findByUserId(requestedUserId);
    }

    @Transactional
    public Goal createGoal(Goal goal) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        if (!isAdmin || goal.getUserId() == null) {
            if (currentUserId == null) {
                throw new BadRequestException("Authentication required to create a goal");
            }
            goal.setUserId(currentUserId);
        }

        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target amount must be strictly greater than 0");
        }

        if (goal.getSavedAmount() == null) {
            goal.setSavedAmount(BigDecimal.ZERO);
        } else if (goal.getSavedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Saved amount cannot be negative");
        }

        if (goal.getTitle() == null || goal.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Goal title is required");
        }
        goal.setTitle(goal.getTitle().trim());

        return goalRepository.save(goal);
    }

    @Transactional
    public Goal updateGoal(Long id, Goal updated) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        Goal existing = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        // Enforce ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(existing.getUserId()))) {
            throw new ForbiddenException("Access denied: You cannot modify this goal");
        }

        if (updated.getTargetAmount() != null) {
            if (updated.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target amount must be strictly greater than 0");
            }
            existing.setTargetAmount(updated.getTargetAmount());
        }

        if (updated.getSavedAmount() != null) {
            if (updated.getSavedAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Saved amount cannot be negative");
            }
            existing.setSavedAmount(updated.getSavedAmount());
        }

        if (updated.getTitle() != null && !updated.getTitle().trim().isEmpty()) {
            existing.setTitle(updated.getTitle().trim());
        }

        if (updated.getTargetDate() != null && !updated.getTargetDate().trim().isEmpty()) {
            existing.setTargetDate(updated.getTargetDate().trim());
        }

        if (updated.getIcon() != null) existing.setIcon(updated.getIcon());
        if (updated.getColor() != null) existing.setColor(updated.getColor());

        return goalRepository.save(existing);
    }

    @Transactional
    public void deleteGoal(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        Goal existing = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        // Enforce ownership
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(existing.getUserId()))) {
            throw new ForbiddenException("Access denied: You cannot delete this goal");
        }

        goalRepository.delete(existing);
    }
}
