package com.warsha.erp.services;

import com.warsha.erp.entities.Category;
import com.warsha.erp.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
    public Category updateCategory(Long id, Category category) {
        category.setCategoryId(id);
        return categoryRepository.save(category);
    }
}
