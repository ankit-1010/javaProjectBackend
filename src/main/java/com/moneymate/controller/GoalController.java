package com.moneymate.controller;

import com.moneymate.dto.ApiResponse;
import com.moneymate.model.Goal;
import com.moneymate.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @GetMapping("/my")
    public ResponseEntity<List<Goal>> getMyGoals() {
        return ResponseEntity.ok(goalService.getCurrentUserGoals());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Goal>> getGoalsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(goalService.getGoalsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Goal> createGoal(@RequestBody Goal goal) {
        return new ResponseEntity<>(goalService.createGoal(goal), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Goal> updateGoal(@PathVariable Long id, @RequestBody Goal goal) {
        return ResponseEntity.ok(goalService.updateGoal(id, goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Goal deleted successfully"));
    }
}
