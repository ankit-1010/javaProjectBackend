package com.moneymate.controller;

import com.moneymate.dto.ApiResponse;
import com.moneymate.model.Budget;
import com.moneymate.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping("/my")
    public ResponseEntity<List<Budget>> getMyBudgets() {
        return ResponseEntity.ok(budgetService.getCurrentUserBudgets());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getBudgetsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(budgetService.getBudgetsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        return new ResponseEntity<>(budgetService.createBudget(budget), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id, @RequestBody Budget budget) {
        return ResponseEntity.ok(budgetService.updateBudget(id, budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Budget deleted successfully"));
    }
}
