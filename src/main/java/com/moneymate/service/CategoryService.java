package com.moneymate.service;

import com.moneymate.exception.BadRequestException;
import com.moneymate.exception.DuplicateResourceException;
import com.moneymate.exception.ResourceNotFoundException;
import com.moneymate.model.Category;
import com.moneymate.repository.CategoryRepository;
import com.moneymate.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Category> getAllCategories() {
        List<Category> list = categoryRepository.findByOrderByNameAsc();
        for (Category c : list) {
            long count = transactionRepository.countByCategory(c.getName());
            c.setTransactionCount((int) count);
        }
        return list;
    }

    @Transactional
    public Category createCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }
        String cleanName = category.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(cleanName)) {
            throw new DuplicateResourceException("Category already exists: " + cleanName);
        }
        category.setName(cleanName);

        if (category.getIcon() == null || category.getIcon().trim().isEmpty()) {
            category.setIcon("Tag");
        }
        if (category.getType() == null || category.getType().trim().isEmpty()) {
            category.setType("EXPENSE");
        }
        if (category.getColor() == null || category.getColor().trim().isEmpty()) {
            category.setColor("#3B82F6");
        }
        if (category.getBudgetLimit() == null) {
            category.setBudgetLimit(BigDecimal.ZERO);
        }
        category.setTransactionCount(0);

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category updated) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (updated.getName() != null && !updated.getName().trim().isEmpty()) {
            String newName = updated.getName().trim();
            if (!newName.equalsIgnoreCase(existing.getName()) && categoryRepository.existsByNameIgnoreCase(newName)) {
                throw new DuplicateResourceException("Category already exists: " + newName);
            }
            existing.setName(newName);
        }

        if (updated.getIcon() != null && !updated.getIcon().trim().isEmpty()) {
            existing.setIcon(updated.getIcon().trim());
        }
        if (updated.getType() != null && !updated.getType().trim().isEmpty()) {
            existing.setType(updated.getType().trim().toUpperCase());
        }
        if (updated.getColor() != null && !updated.getColor().trim().isEmpty()) {
            existing.setColor(updated.getColor().trim());
        }
        if (updated.getBudgetLimit() != null) {
            existing.setBudgetLimit(updated.getBudgetLimit());
        }

        Category saved = categoryRepository.save(existing);
        long count = transactionRepository.countByCategory(saved.getName());
        saved.setTransactionCount((int) count);
        return saved;
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
