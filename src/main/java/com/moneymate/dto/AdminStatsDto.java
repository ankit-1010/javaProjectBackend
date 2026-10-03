package com.moneymate.dto;

import com.moneymate.model.Transaction;
import com.moneymate.model.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminStatsDto {
    private long totalUsers = 0;
    private long activeUsers = 0;
    private long inactiveUsers = 0;
    private long totalTransactions = 0;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    private double usersGrowthPercent = 0.0;
    private double transactionsGrowthPercent = 0.0;
    private double incomeGrowthPercent = 0.0;
    private double expensesGrowthPercent = 0.0;

    private List<User> recentUsers = new ArrayList<>();
    private List<Transaction> recentTransactions = new ArrayList<>();
    private Map<String, BigDecimal> expenseBreakdown = new LinkedHashMap<>();
    private Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();

    public AdminStatsDto() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }

    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public double getUsersGrowthPercent() { return usersGrowthPercent; }
    public void setUsersGrowthPercent(double usersGrowthPercent) { this.usersGrowthPercent = usersGrowthPercent; }

    public double getTransactionsGrowthPercent() { return transactionsGrowthPercent; }
    public void setTransactionsGrowthPercent(double transactionsGrowthPercent) { this.transactionsGrowthPercent = transactionsGrowthPercent; }

    public double getIncomeGrowthPercent() { return incomeGrowthPercent; }
    public void setIncomeGrowthPercent(double incomeGrowthPercent) { this.incomeGrowthPercent = incomeGrowthPercent; }

    public double getExpensesGrowthPercent() { return expensesGrowthPercent; }
    public void setExpensesGrowthPercent(double expensesGrowthPercent) { this.expensesGrowthPercent = expensesGrowthPercent; }

    public List<User> getRecentUsers() { return recentUsers; }
    public void setRecentUsers(List<User> recentUsers) { this.recentUsers = recentUsers; }

    public List<Transaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<Transaction> recentTransactions) { this.recentTransactions = recentTransactions; }

    public Map<String, BigDecimal> getExpenseBreakdown() { return expenseBreakdown; }
    public void setExpenseBreakdown(Map<String, BigDecimal> expenseBreakdown) { this.expenseBreakdown = expenseBreakdown; }

    public Map<String, BigDecimal> getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(Map<String, BigDecimal> monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
}
