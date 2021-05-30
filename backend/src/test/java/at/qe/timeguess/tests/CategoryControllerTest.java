package at.qe.timeguess.tests;

import at.qe.timeguess.controllers.CategoryController;
import at.qe.timeguess.dto.CategoryInfoDTO;
import at.qe.timeguess.dto.NameDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.services.CategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
public class CategoryControllerTest {

    @Autowired
    private CategoryController categoryController;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testGetAllCategories() {
        ResponseEntity<List<Category>> response = this.categoryController.getAllCategories();
        List<Category> allCategories = response.getBody();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(8, allCategories.size());
    }

    @Test
    public void testGetAllCategoriesWithInfo() {
        ResponseEntity<List<CategoryInfoDTO>> response = this.categoryController.getAllCategoriesWithInfo();
        List<CategoryInfoDTO> allCategoriesWithInfo = response.getBody();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(8, allCategoriesWithInfo.size());
        Assertions.assertEquals("Deutschland", allCategoriesWithInfo.get(0).getName());
    }

    @Test
    public void testGetCategoryByIdHttpStatusOk() {
        ResponseEntity<Category> response = this.categoryController.getCategoryById(0L);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Deutschland", response.getBody().getName());
    }

    @Test
    public void testGetCategoryByIdHttpStatusNotFound() {
        ResponseEntity<Category> response = this.categoryController.getCategoryById(100L);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testCreateCategoryHttpStatusCreated() {
        NameDTO nameDTO = new NameDTO("Politics");
        ResponseEntity<Category> response = this.categoryController.createCategory(nameDTO);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("Politics", response.getBody().getName());
        Assertions.assertEquals(9, this.categoryService.getAllCategories().size());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testCreateCategoryHttpStatusConflict() {
        NameDTO nameDTO = new NameDTO("Deutschland");
        ResponseEntity<Category> response = this.categoryController.createCategory(nameDTO);

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertEquals(8, this.categoryService.getAllCategories().size());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testDeleteCategoryHttpStatusOk() {
        NameDTO nameDTO = new NameDTO("Politics");
        this.categoryController.createCategory(nameDTO);
        ResponseEntity<Category> response = this.categoryController.deleteCategory(11L);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testDeleteCategoryHttpStatusForbidden() {
        ResponseEntity<Category> response = this.categoryController.deleteCategory(2L);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testDeleteCategoryHttpStatusNotFound() {
        ResponseEntity<Category> response = this.categoryController.deleteCategory(100L);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
