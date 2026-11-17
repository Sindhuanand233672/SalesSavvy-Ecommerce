
package com.salessavvy.service;

import java.util.List;

import com.salessavvy.entity.Category;

public interface CategoryService {

    Category createCategory(Category category);

    List<Category> getAllCategories();

    List<Category> getAllCategoriesForAdmin();

    Category getCategoryById(Long id);

    Category updateCategory(Long id, Category category);

    void deactivateCategory(Long id);
}
