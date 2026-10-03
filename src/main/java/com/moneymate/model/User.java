package com.moneymate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role = "USER"; // USER or ADMIN

    @Column(nullable = false, length = 20)
    private String status = "Active"; // Active or Inactive

    @Column(length = 30)
    private String phone;

    @Column
    private String avatar = "A";

    @Column(nullable = false)
    private LocalDate joinedOn = LocalDate.now();

    @Column(precision = 12, scale = 2)
    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private Long txCount = 0L;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (joinedOn == null) {
            joinedOn = LocalDate.now();
        }
        if (totalBalance == null) {
            totalBalance = BigDecimal.ZERO;
        }
        if (avatar == null || avatar.trim().isEmpty()) {
            avatar = fullName != null && !fullName.isEmpty() ? fullName.substring(0, 1).toUpperCase() : "U";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User() {}

    public User(String fullName, String email, String password, String role, String status, String phone, String avatar, LocalDate joinedOn, BigDecimal totalBalance) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : "USER";
        this.status = status != null ? status : "Active";
        this.phone = phone;
        this.avatar = avatar;
        this.joinedOn = joinedOn != null ? joinedOn : LocalDate.now();
        this.totalBalance = totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public LocalDate getJoinedOn() { return joinedOn; }
    public void setJoinedOn(LocalDate joinedOn) { this.joinedOn = joinedOn; }

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getTxCount() { return txCount; }
    public void setTxCount(Long txCount) { this.txCount = txCount; }
}
