package com.moneymate.service;

import com.moneymate.dto.AdminStatsDto;
import com.moneymate.dto.UserOverviewDto;
import com.moneymate.model.Budget;
import com.moneymate.model.Transaction;
import com.moneymate.model.User;
import com.moneymate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private BudgetService budgetService;

    public AdminStatsDto getAdminStats() {
        AdminStatsDto stats = new AdminStatsDto();

        long usersCount = userRepository.count();
        long activeCount = userRepository.countByStatus("Active");
        long inactiveCount = userRepository.countByStatus("Inactive");
        long txnsCount = transactionRepository.count();

        stats.setTotalUsers(usersCount);
        stats.setActiveUsers(activeCount);
        stats.setInactiveUsers(inactiveCount);
        stats.setTotalTransactions(txnsCount);

        BigDecimal income = transactionRepository.sumTotalIncome();
        BigDecimal expenses = transactionRepository.sumTotalExpenses();

        stats.setTotalIncome(income != null ? income : BigDecimal.ZERO);
        stats.setTotalExpenses(expenses != null ? expenses : BigDecimal.ZERO);

        // Real growth rate calculation: This month vs Previous month
        LocalDate today = LocalDate.now();
        YearMonth currentYm = YearMonth.from(today);
        YearMonth prevYm = currentYm.minusMonths(1);

        LocalDate currentStart = currentYm.atDay(1);
        LocalDate prevStart = prevYm.atDay(1);
        LocalDate prevEnd = prevYm.atEndOfMonth();

        long curUsers = userRepository.countByJoinedOnBetween(currentStart, today);
        long prevUsers = userRepository.countByJoinedOnBetween(prevStart, prevEnd);
        stats.setUsersGrowthPercent(calcGrowth(curUsers, prevUsers));

        long curTxns = transactionRepository.countByTransactionDateBetween(currentStart, today);
        long prevTxns = transactionRepository.countByTransactionDateBetween(prevStart, prevEnd);
        stats.setTransactionsGrowthPercent(calcGrowth(curTxns, prevTxns));

        BigDecimal curIncome = transactionRepository.sumIncomeBetween(currentStart, today);
        BigDecimal prevIncome = transactionRepository.sumIncomeBetween(prevStart, prevEnd);
        stats.setIncomeGrowthPercent(calcGrowth(curIncome, prevIncome));

        BigDecimal curExp = transactionRepository.sumExpensesBetween(currentStart, today);
        BigDecimal prevExp = transactionRepository.sumExpensesBetween(prevStart, prevEnd);
        stats.setExpensesGrowthPercent(calcGrowth(curExp, prevExp));

        // Recent users with their transaction counts
        List<User> topUsers = userRepository.findTop5ByOrderByJoinedOnDesc();
        for (User u : topUsers) {
            u.setTxCount(transactionRepository.countByUserId(u.getId()));
        }
        stats.setRecentUsers(topUsers);

        // Recent transactions
        List<Transaction> topTxns = transactionRepository.findTop5ByOrderByTransactionDateDesc();
        stats.setRecentTransactions(topTxns);

        // Real Expense Breakdown by Category
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        List<Object[]> categorySums = transactionRepository.sumExpenseGroupedByCategory();
        for (Object[] row : categorySums) {
            if (row != null && row.length >= 2 && row[0] != null) {
                String catName = (String) row[0];
                BigDecimal sum = (BigDecimal) row[1];
                breakdown.put(catName, sum != null ? sum : BigDecimal.ZERO);
            }
        }
        stats.setExpenseBreakdown(breakdown);

        // Real Monthly Revenue (Income) for last 6 months
        Map<String, BigDecimal> monthlyRev = new LinkedHashMap<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYm.minusMonths(i);
            BigDecimal mIncome = transactionRepository.sumIncomeBetween(ym.atDay(1), ym.atEndOfMonth());
            monthlyRev.put(ym.format(monthFmt), mIncome != null ? mIncome : BigDecimal.ZERO);
        }
        stats.setMonthlyRevenue(monthlyRev);

        return stats;
    }

    public UserOverviewDto getUserOverview(Long userId) {
        UserOverviewDto overview = new UserOverviewDto();

        User user = userRepository.findById(userId).orElse(null);
        BigDecimal totalBal = (user != null && user.getTotalBalance() != null) ? user.getTotalBalance() : BigDecimal.ZERO;
        overview.setTotalBalance(totalBal);

        BigDecimal income = transactionRepository.sumUserIncome(userId);
        BigDecimal expense = transactionRepository.sumUserExpense(userId);

        BigDecimal finalIncome = income != null ? income : BigDecimal.ZERO;
        BigDecimal finalExpense = expense != null ? expense : BigDecimal.ZERO;

        overview.setTotalIncome(finalIncome);
        overview.setTotalExpenses(finalExpense);
        overview.setTotalSavings(finalIncome.compareTo(finalExpense) > 0 ? finalIncome.subtract(finalExpense) : BigDecimal.ZERO);

        overview.setRecentTransactions(transactionRepository.findByUserIdOrderByTransactionDateDesc(userId));

        // Use budgetService to derive accurate spent amount
        List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
        overview.setBudgets(budgets);

        overview.setGoals(goalRepository.findByUserId(userId));

        // Real user expense breakdown
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        List<Object[]> userCatSums = transactionRepository.sumUserExpenseGroupedByCategory(userId);
        for (Object[] row : userCatSums) {
            if (row != null && row.length >= 2 && row[0] != null) {
                String catName = (String) row[0];
                BigDecimal sum = (BigDecimal) row[1];
                breakdown.put(catName, sum != null ? sum : BigDecimal.ZERO);
            }
        }
        overview.setExpenseBreakdown(breakdown);

        // Real balance change comparison
        overview.setBalanceChangePercent(finalIncome.compareTo(BigDecimal.ZERO) > 0 && finalExpense.compareTo(BigDecimal.ZERO) > 0
                ? ((finalIncome.subtract(finalExpense)).divide(finalIncome, 4, RoundingMode.HALF_UP)).doubleValue() * 100 : 0.0);

        return overview;
    }

    private double calcGrowth(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        double val = ((double) (current - previous) / previous) * 100.0;
        return Math.round(val * 10.0) / 10.0;
    }

    private double calcGrowth(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return (current != null && current.compareTo(BigDecimal.ZERO) > 0) ? 100.0 : 0.0;
        }
        if (current == null) {
            current = BigDecimal.ZERO;
        }
        BigDecimal diff = current.subtract(previous);
        BigDecimal growth = diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return Math.round(growth.doubleValue() * 10.0) / 10.0;
    }
}
