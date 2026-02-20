package com.musicstreaming.dao;

import com.musicstreaming.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class SubscriptionDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SubscriptionDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Subscription> subscriptionRowMapper = (rs, rowNum) -> {
        Subscription subscription = new Subscription();
        subscription.setId(rs.getInt("Id"));
        subscription.setUserId(rs.getInt("UserId"));
        subscription.setStartDate(rs.getTimestamp("StartDate").toLocalDateTime());
        subscription.setEndDate(rs.getTimestamp("EndDate").toLocalDateTime());
        subscription.setActivated(rs.getBoolean("IsActivated"));
        subscription.setTransactionId(rs.getString("TransactionId"));
        subscription.setAmount(rs.getBigDecimal("Amount"));
        subscription.setStatus(rs.getString("Status"));
        return subscription;
    };

    public Optional<Subscription> findById(Integer id) {
        String sql = "SELECT * FROM Subscriptions WHERE Id = ?";
        try {
            Subscription subscription = jdbcTemplate.queryForObject(sql, subscriptionRowMapper, id);
            return Optional.ofNullable(subscription);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Subscription> findByUserId(Integer userId) {
        String sql = "SELECT * FROM Subscriptions WHERE UserId = ? ORDER BY StartDate DESC";
        return jdbcTemplate.query(sql, subscriptionRowMapper, userId);
    }

    public Optional<Subscription> findActiveByUserId(Integer userId) {
        String sql = "SELECT * FROM Subscriptions WHERE UserId = ? AND IsActivated = TRUE AND EndDate > NOW() ORDER BY EndDate DESC LIMIT 1";
        try {
            Subscription subscription = jdbcTemplate.queryForObject(sql, subscriptionRowMapper, userId);
            return Optional.ofNullable(subscription);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Subscription> findActiveSubscriptions() {
        String sql = "SELECT * FROM Subscriptions WHERE IsActivated = TRUE AND EndDate > NOW() ORDER BY EndDate";
        return jdbcTemplate.query(sql, subscriptionRowMapper);
    }

    public Subscription save(Subscription subscription) {
        if (subscription.getId() == null) {
            return insert(subscription);
        } else {
            return update(subscription);
        }
    }

    private Subscription insert(Subscription subscription) {
        String sql = "INSERT INTO Subscriptions (UserId, StartDate, EndDate, IsActivated, TransactionId, Amount, Status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, subscription.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(subscription.getStartDate()));
            ps.setTimestamp(3, Timestamp.valueOf(subscription.getEndDate()));
            ps.setBoolean(4, subscription.isActivated());
            ps.setString(5, subscription.getTransactionId());
            ps.setBigDecimal(6, subscription.getAmount());
            ps.setString(7, subscription.getStatus());
            return ps;
        }, keyHolder);

        subscription.setId(keyHolder.getKey().intValue());
        return subscription;
    }

    private Subscription update(Subscription subscription) {
        String sql = "UPDATE Subscriptions SET StartDate = ?, EndDate = ?, IsActivated = ?, " +
                "TransactionId = ?, Amount = ?, Status = ? WHERE Id = ?";
        jdbcTemplate.update(sql,
                Timestamp.valueOf(subscription.getStartDate()),
                Timestamp.valueOf(subscription.getEndDate()),
                subscription.isActivated(),
                subscription.getTransactionId(),
                subscription.getAmount(),
                subscription.getStatus(),
                subscription.getId()
        );
        return subscription;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Subscriptions WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }
}