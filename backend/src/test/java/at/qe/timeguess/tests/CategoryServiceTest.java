package at.qe.timeguess.tests;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.services.CategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;

@SpringBootTest
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testGetAllCategories() {
        Collection<Category> allCategories = this.categoryService.getAllCategories();
        Assertions.assertEquals(1, allCategories.size());
        Assertions.assertTrue(allCategories.stream().anyMatch(category -> category.getName().equals("Deutschland")));
    }

    @Test
    public void testGetCategoryById() {
        Category category = this.categoryService.getCategoryById(0L);
        Assertions.assertEquals("Deutschland", category.getName());
    }

    @Test
    public void testGetCategoryByName() {
        Category category = this.categoryService.getCategoryByName("Deutschland");
        Assertions.assertEquals(0L, category.getId());
    }

    @Test
    @DirtiesContext
    public void testSaveCategory() throws CategoryService.CategoryAlreadyExistsException {
        Category categoryToSave = new Category("Politics");
        this.categoryService.saveCategory(categoryToSave);

        Collection<Category> allCategories = this.categoryService.getAllCategories();

        Assertions.assertEquals(2, allCategories.size());
        Assertions.assertTrue(allCategories.stream().anyMatch(category -> category.getName().equals("Politics")));
    }

    @Test
    @DirtiesContext
    public void testDeleteCategory() throws CategoryService.CategoryIsReferencedInCompletedGamesException, CategoryService.CategoryAlreadyExistsException {
        Category categoryToSave = new Category("Politics");
        this.categoryService.saveCategory(categoryToSave);

        Category categoryToDelete = this.categoryService.getCategoryById(1L);
        this.categoryService.deleteCategory(categoryToDelete);

        Collection<Category> allCategories = this.categoryService.getAllCategories();

        Assertions.assertEquals(1, allCategories.size());
        Assertions.assertTrue(allCategories.stream().noneMatch(category -> category.getName().equals("Politics")));
    }

    @Test
    @DirtiesContext
    public void testCategoryAlreadyExistsExceptionBySaving() throws CategoryService.CategoryAlreadyExistsException {
        Category categoryToSave = new Category("Deutschland");
        Assertions.assertThrows(CategoryService.CategoryAlreadyExistsException.class, () -> this.categoryService.saveCategory(categoryToSave));

    }

    @Test
    @DirtiesContext
    public void testCategoryIsReferencedInCompletedGameException() throws CategoryService.CategoryIsReferencedInCompletedGamesException {
        Category categoryToDelete = this.categoryService.getCategoryById(0L);
        Assertions.assertThrows(CategoryService.CategoryIsReferencedInCompletedGamesException.class, () -> this.categoryService.deleteCategory(categoryToDelete));
    }
}
