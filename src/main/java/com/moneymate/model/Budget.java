package com.moneymate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "spent_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(name = "allocated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "month_year", nullable = false, length = 20)
    private String monthYear = "September 2026";

    @Column(length = 50)
    private String icon = "Wallet";

    @Column(length = 20)
    private String color = "#3B82F6";

    @Transient
    public BigDecimal getRemainingAmount() {
        if (allocatedAmount == null) return BigDecimal.ZERO;
        BigDecimal spent = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        return allocatedAmount.subtract(spent);
    }

    @Transient
    public double getUsagePercentage() {
        if (allocatedAmount == null || allocatedAmount.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        BigDecimal spent = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        return spent.divide(allocatedAmount, 4, RoundingMode.HALF_UP).doubleValue() * 100;
    }

    public Budget() {}

    public Budget(Long userId, String category, BigDecimal spentAmount, BigDecimal allocatedAmount, String monthYear, String icon, String color) {
        this.userId = userId;
        this.category = category;
        this.spentAmount = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        this.allocatedAmount = allocatedAmount;
        this.monthYear = monthYear != null ? monthYear : "September 2026";
        this.icon = icon != null ? icon : "Wallet";
        this.color = color != null ? color : "#3B82F6";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
