package com.musicstreaming.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Subscription {
    private Integer id;
    private Integer userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActivated;
    private String transactionId;
    private BigDecimal amount;
    private String status;

    // Related objects
    private User user;

    // Constructors
    public Subscription() {
        this.startDate = LocalDateTime.now();
        this.endDate = startDate.plusMonths(1);
        this.amount = new BigDecimal("399.00");
        this.status = "pending";
    }

    public Subscription(Integer userId) {
        this();
        this.userId = userId;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public boolean isActivated() { return isActivated; }
    public void setActivated(boolean activated) { isActivated = activated; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}