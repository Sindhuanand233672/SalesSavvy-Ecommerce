
package com.salessavvy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.entity.Category;
import com.salessavvy.repository.CategoryRepository;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private void validateCategory(Category category) {

        if (category == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category details are required"
            );
        }

        if (category.getName() == null || category.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category name is required"
            );
        }

        if (category.getName().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category name cannot exceed 100 characters"
            );
        }

        if (category.getDescription() != null
                && category.getDescription().length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description cannot exceed 500 characters"
            );
        }
    }

    @Override
    public Category createCategory(Category category) {

        validateCategory(category);

        String name = category.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists"
            );
        }

        category.setName(name);
        category.setActive(true);

        return categoryRepository.save(category);
    }

    // Public: active categories only
    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {

        return categoryRepository.findByActiveTrue();
    }

    // Admin: active and inactive categories
    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForAdmin() {

        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found with ID: " + id
                        )
                );
    }

    @Override
    public Category updateCategory(Long id, Category updatedCategory) {

        validateCategory(updatedCategory);

        Category existingCategory = getCategoryById(id);

        String newName = updatedCategory.getName().trim();

        if (!existingCategory.getName().equalsIgnoreCase(newName)
                && categoryRepository.existsByNameIgnoreCase(newName)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists"
            );
        }

        existingCategory.setName(newName);
        existingCategory.setDescription(updatedCategory.getDescription());

        // Important: allows both activation and deactivation through PUT
        if (updatedCategory.getActive() != null) {
            existingCategory.setActive(updatedCategory.getActive());
        }

        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deactivateCategory(Long id) {

        Category category = getCategoryById(id);

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category is already inactive"
            );
        }

        category.setActive(false);

        categoryRepository.save(category);
    }
}
