package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse getAllCategories();

    void createCategory(Category category);

    public String deleteCategory(Long categoryId);

    Category updateCategory(Category category, Long categoryId);
}
