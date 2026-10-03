package com.moneymate.repository;

import com.moneymate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByStatus(String status);
    long countByRole(String role);
    long countByStatus(String status);
    long countByJoinedOnBetween(LocalDate start, LocalDate end);
    long countByJoinedOnBefore(LocalDate date);
    List<User> findTop5ByOrderByJoinedOnDesc();
    List<User> findByOrderByJoinedOnDesc();
}
