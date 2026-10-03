package com.moneymate.repository;

import com.moneymate.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndTypeOrderByTransactionDateDesc(Long userId, String type);

    List<Transaction> findByOrderByTransactionDateDesc();

    List<Transaction> findTop5ByOrderByTransactionDateDesc();

    long countByUserId(Long userId);

    long countByCategory(String category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE LOWER(t.type) = 'income'")
    BigDecimal sumTotalIncome();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE LOWER(t.type) = 'expense'")
    BigDecimal sumTotalExpenses();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND LOWER(t.type) = 'income'")
    BigDecimal sumUserIncome(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND LOWER(t.type) = 'expense'")
    BigDecimal sumUserExpense(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND LOWER(t.type) = 'expense' AND LOWER(t.category) = LOWER(:category)")
    BigDecimal sumUserExpenseByCategory(@Param("userId") Long userId, @Param("category") String category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND LOWER(t.type) = 'expense' AND LOWER(t.category) = LOWER(:category) AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumUserExpenseByCategoryAndDateBetween(@Param("userId") Long userId, @Param("category") String category, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE LOWER(t.type) = 'expense' GROUP BY t.category")
    List<Object[]> sumExpenseGroupedByCategory();

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND LOWER(t.type) = 'expense' GROUP BY t.category")
    List<Object[]> sumUserExpenseGroupedByCategory(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end")
    long countByTransactionDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE LOWER(t.type) = 'income' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumIncomeBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE LOWER(t.type) = 'expense' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumExpensesBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
