package com.moneymate.repository;

import com.moneymate.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(Long userId);
    List<Budget> findByUserIdAndMonthYear(Long userId, String monthYear);
    Optional<Budget> findByUserIdAndCategory(Long userId, String category);
    boolean existsByUserIdAndCategory(Long userId, String category);
}
