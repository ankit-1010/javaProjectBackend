package com.moneymate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "saved_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;

    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "target_date", nullable = false, length = 50)
    private String targetDate;

    @Column(length = 50)
    private String icon = "Target";

    @Column(length = 20)
    private String color = "#3B82F6";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Transient
    public double getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        BigDecimal saved = savedAmount != null ? savedAmount : BigDecimal.ZERO;
        return saved.divide(targetAmount, 4, RoundingMode.HALF_UP).doubleValue() * 100;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (savedAmount == null) {
            savedAmount = BigDecimal.ZERO;
        }
        if (icon == null || icon.trim().isEmpty()) {
            icon = "Target";
        }
        if (color == null || color.trim().isEmpty()) {
            color = "#3B82F6";
        }
    }

    public Goal() {}

    public Goal(Long userId, String title, BigDecimal savedAmount, BigDecimal targetAmount, String targetDate, String icon, String color) {
        this.userId = userId;
        this.title = title;
        this.savedAmount = savedAmount != null ? savedAmount : BigDecimal.ZERO;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.icon = icon != null ? icon : "Target";
        this.color = color != null ? color : "#3B82F6";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getSavedAmount() { return savedAmount; }
    public void setSavedAmount(BigDecimal savedAmount) { this.savedAmount = savedAmount; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
