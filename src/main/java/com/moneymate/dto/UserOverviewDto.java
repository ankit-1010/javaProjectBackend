package com.moneymate.dto;

import com.moneymate.model.Budget;
import com.moneymate.model.Goal;
import com.moneymate.model.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserOverviewDto {
    private BigDecimal totalBalance = BigDecimal.ZERO;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal totalSavings = BigDecimal.ZERO;
    private double balanceChangePercent = 0.0;
    private List<Transaction> recentTransactions = new ArrayList<>();
    private List<Budget> budgets = new ArrayList<>();
    private List<Goal> goals = new ArrayList<>();
    private Map<String, BigDecimal> expenseBreakdown = new LinkedHashMap<>();

    public UserOverviewDto() {}

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getTotalSavings() { return totalSavings; }
    public void setTotalSavings(BigDecimal totalSavings) { this.totalSavings = totalSavings; }

    public double getBalanceChangePercent() { return balanceChangePercent; }
    public void setBalanceChangePercent(double balanceChangePercent) { this.balanceChangePercent = balanceChangePercent; }

    public List<Transaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<Transaction> recentTransactions) { this.recentTransactions = recentTransactions; }

    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }

    public List<Goal> getGoals() { return goals; }
    public void setGoals(List<Goal> goals) { this.goals = goals; }

    public Map<String, BigDecimal> getExpenseBreakdown() { return expenseBreakdown; }
    public void setExpenseBreakdown(Map<String, BigDecimal> expenseBreakdown) { this.expenseBreakdown = expenseBreakdown; }
}
