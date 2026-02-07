package com.warsha.erp.controllers;

import com.warsha.erp.entities.Category;
import com.warsha.erp.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAllCategories();
    }

    @PostMapping("/add")
    public Category addCategory(@RequestBody Category category) {
        return categoryService.saveCategory(category);
    }
}
