package com.moneymate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String icon = "Tag";

    @Column(nullable = false, length = 20)
    private String type = "EXPENSE"; // EXPENSE, INCOME, BOTH

    @Column(length = 20)
    private String color = "#3B82F6";

    @Column(name = "transaction_count")
    private Integer transactionCount = 0;

    @Column(name = "budget_limit", precision = 12, scale = 2)
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate() {
        if (transactionCount == null) {
            transactionCount = 0;
        }
        if (budgetLimit == null) {
            budgetLimit = BigDecimal.ZERO;
        }
        if (icon == null || icon.trim().isEmpty()) {
            icon = "Tag";
        }
        if (type == null || type.trim().isEmpty()) {
            type = "EXPENSE";
        }
        if (color == null || color.trim().isEmpty()) {
            color = "#3B82F6";
        }
    }

    public Category() {}

    public Category(String name, String icon, String type, String color, Integer transactionCount, BigDecimal budgetLimit) {
        this.name = name;
        this.icon = icon != null ? icon : "Tag";
        this.type = type != null ? type : "EXPENSE";
        this.color = color != null ? color : "#3B82F6";
        this.transactionCount = transactionCount != null ? transactionCount : 0;
        this.budgetLimit = budgetLimit != null ? budgetLimit : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }
}
