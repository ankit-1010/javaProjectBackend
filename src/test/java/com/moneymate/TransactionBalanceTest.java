package com.moneymate;

import com.moneymate.exception.BadRequestException;
import com.moneymate.model.Transaction;
import com.moneymate.model.User;
import com.moneymate.repository.CategoryRepository;
import com.moneymate.repository.TransactionRepository;
import com.moneymate.repository.UserRepository;
import com.moneymate.security.SecurityUtils;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionBalanceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
        mockedSecurityUtils.when(SecurityUtils::isCurrentUserAdmin).thenReturn(false);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setTotalBalance(new BigDecimal("10000.00"));
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtils != null) {
            mockedSecurityUtils.close();
        }
    }

    @Test
    void testAddIncomeIncreasesBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(101L);
            return t;
        });

        Transaction t = new Transaction();
        t.setType("Income");
        t.setAmount(new BigDecimal("5000.00"));
        t.setTransactionDate(LocalDate.now());

        Transaction created = transactionService.createTransaction(t);
        assertNotNull(created);
        assertEquals(new BigDecimal("15000.00"), testUser.getTotalBalance());
    }

    @Test
    void testEditIncomeRecalculatesBalance() {
        Transaction existing = new Transaction();
        existing.setId(101L);
        existing.setUserId(1L);
        existing.setType("Income");
        existing.setAmount(new BigDecimal("5000.00"));
        existing.setTransactionDate(LocalDate.now());

        // Balance after creation was 15000
        testUser.setTotalBalance(new BigDecimal("15000.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(101L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction updateReq = new Transaction();
        updateReq.setType("Income");
        updateReq.setAmount(new BigDecimal("8000.00"));
        updateReq.setDescription("Updated Salary");

        Transaction updated = transactionService.updateTransaction(101L, updateReq);
        assertNotNull(updated);
        // 15000 - 5000 + 8000 = 18000
        assertEquals(new BigDecimal("18000.00"), testUser.getTotalBalance());
    }

    @Test
    void testDeleteIncomeReversesEffect() {
        Transaction existing = new Transaction();
        existing.setId(101L);
        existing.setUserId(1L);
        existing.setType("Income");
        existing.setAmount(new BigDecimal("8000.00"));
        existing.setTransactionDate(LocalDate.now());

        // Balance after edit was 18000
        testUser.setTotalBalance(new BigDecimal("18000.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(101L)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(101L);
        // 18000 - 8000 = 10000
        assertEquals(new BigDecimal("10000.00"), testUser.getTotalBalance());
        verify(transactionRepository, times(1)).delete(existing);
    }

    @Test
    void testAddExpenseDecreasesBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction t = new Transaction();
        t.setType("Expense");
        t.setAmount(new BigDecimal("3000.00"));
        t.setTransactionDate(LocalDate.now());

        transactionService.createTransaction(t);
        // 10000 - 3000 = 7000
        assertEquals(new BigDecimal("7000.00"), testUser.getTotalBalance());
    }

    @Test
    void testNegativeOrZeroAmountThrowsBadRequest() {
        Transaction zeroTx = new Transaction();
        zeroTx.setType("Income");
        zeroTx.setAmount(BigDecimal.ZERO);
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(zeroTx));

        Transaction negTx = new Transaction();
        negTx.setType("Income");
        negTx.setAmount(new BigDecimal("-100.00"));
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(negTx));
    }
}
