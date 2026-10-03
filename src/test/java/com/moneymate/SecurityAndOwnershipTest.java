package com.moneymate;

import com.moneymate.exception.ForbiddenException;
import com.moneymate.model.Budget;
import com.moneymate.model.Transaction;
import com.moneymate.model.User;
import com.moneymate.repository.BudgetRepository;
import com.moneymate.repository.TransactionRepository;
import com.moneymate.security.SecurityUtils;
import com.moneymate.service.BudgetService;
import com.moneymate.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityAndOwnershipTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    @InjectMocks
    private TransactionService transactionService;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtils != null) {
            mockedSecurityUtils.close();
        }
    }

    @Test
    void testBCryptPasswordHashing() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "mySecretPassword123";
        String encoded = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }

    @Test
    void testOwnershipCheckBlocksUnauthorizedAccess() {
        // Current user is User ID 1 (not admin)
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
        mockedSecurityUtils.when(SecurityUtils::isCurrentUserAdmin).thenReturn(false);

        // Attempting to access User ID 2's transactions
        assertThrows(ForbiddenException.class, () -> {
            transactionService.getTransactionsByUserIdAndType(2L, "All");
        });
    }

    @Test
    void testBudgetCalculatesSpentFromTransactions() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
        mockedSecurityUtils.when(SecurityUtils::isCurrentUserAdmin).thenReturn(false);

        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUserId(1L);
        budget.setCategory("Food");
        budget.setAllocatedAmount(new BigDecimal("10000.00"));

        when(budgetRepository.findByUserId(1L)).thenReturn(Collections.singletonList(budget));
        when(transactionRepository.sumUserExpenseByCategory(1L, "Food"))
                .thenReturn(new BigDecimal("7500.00"));

        List<Budget> budgets = budgetService.getBudgetsByUserId(1L);
        assertEquals(1, budgets.size());
        Budget b = budgets.get(0);

        assertEquals(new BigDecimal("7500.00"), b.getSpentAmount());
        assertEquals(new BigDecimal("2500.00"), b.getRemainingAmount());
        assertEquals(75.0, b.getUsagePercentage());
    }
}
