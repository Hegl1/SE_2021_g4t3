package at.qe.timeguess.services;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Collection<Category> getAllCategories() {
        return this.categoryRepository.findAll();
    }

    public Category getCategoryById(final Long id) {
        return this.categoryRepository.findFirstById(id);
    }

    public Category getCategoryByName(final String category) {
        return this.categoryRepository.findFirstByCategory(category);
    }

    public Category saveCategory(final Category category) {
        return this.categoryRepository.save(category);
    }

    public void deleteCategory(final Category category) {
        this.categoryRepository.delete(category);
    }
}
