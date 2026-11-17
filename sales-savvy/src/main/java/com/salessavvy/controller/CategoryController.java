
package com.salessavvy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.entity.Category;
import com.salessavvy.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Public: get active categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }

    // Admin: get all categories, including inactive
    @GetMapping("/admin/all")
    public ResponseEntity<List<Category>> getAllCategoriesForAdmin() {

        return ResponseEntity.ok(
                categoryService.getAllCategoriesForAdmin()
        );
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(id)
        );
    }

    // Admin: create category
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody Category category) {

        Category savedCategory =
                categoryService.createCategory(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCategory);
    }

    // Admin: update or activate category
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {

        return ResponseEntity.ok(
                categoryService.updateCategory(id, category)
        );
    }

    // Admin: soft delete / deactivate category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable Long id) {

        categoryService.deactivateCategory(id);

        return ResponseEntity.noContent().build();
    }
}
