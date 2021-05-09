package at.qe.timeguess.tests;

import at.qe.timeguess.controllers.ExpressionController;
import at.qe.timeguess.dto.CategoryExpressionAsStringsDTO;
import at.qe.timeguess.dto.CategoryExpressionDTO;
import at.qe.timeguess.dto.ExpressionDTO;
import at.qe.timeguess.dto.NameDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.services.CategoryService;
import at.qe.timeguess.services.ExpressionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.util.LinkedList;
import java.util.List;

@SpringBootTest
public class ExpressionControllerTest {

    @Autowired
    private ExpressionController expressionController;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testGetAllExpressionsOfCategoryHttpStatusOk() {
        ResponseEntity<List<ExpressionDTO>> response = this.expressionController.getAllExpressionsOfCategory(0L);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Bundestag", response.getBody().get(0).getName());
    }

    @Test
    public void testGetAllExpressionsOfCategoryHttpStatusNotFound() {
        ResponseEntity<List<ExpressionDTO>> response = this.expressionController.getAllExpressionsOfCategory(5L);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testCreateExpressionHttpStatusOk() {
        NameDTO nameDTO = new NameDTO("Stefan Raab");
        ResponseEntity<ExpressionDTO> response = this.expressionController.createExpression(0L, nameDTO);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Stefan Raab", response.getBody().getName());
        Assertions.assertEquals(31, this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryById(0L)).size());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testCreateExpressionHttpStatusNotFound() {
        NameDTO nameDTO = new NameDTO("Stefan Raab");
        ResponseEntity<ExpressionDTO> response = this.expressionController.createExpression(5L, nameDTO);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testCreateExpressionHttpStatusConflict() {
        NameDTO nameDTO = new NameDTO("Bundestag");
        ResponseEntity<ExpressionDTO> response = this.expressionController.createExpression(0L, nameDTO);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testDeleteExpressionHttpStatusOk() throws ExpressionService.ExpressionAlreadyExists, CategoryService.CategoryAlreadyExistsException {
        Category category = this.categoryService.saveCategory(new Category("Politics"));
        NameDTO nameDTO = new NameDTO("Trump");
        this.expressionService.saveExpression(11L, nameDTO);
        ResponseEntity<Expression> response = this.expressionController.deleteExpression(106L);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(0, this.expressionService.getAllExpressionsByCategory(category).size());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testDeleteExpressionHttpStatusNotFound() {
        ResponseEntity<Expression> response = this.expressionController.deleteExpression(500L);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testImportExpressionsIntoCategory() {
        List<String> expressionNames = new LinkedList<>();
        for(int i = 1; i < 6; i++) {
            expressionNames.add("Expression " + i);
        }

        ResponseEntity<List<ExpressionDTO>> response = this.expressionController.importExpressionsIntoCategory(0L, expressionNames);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(35, this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryById(0L)).size());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = { "ADMIN" })
    public void testImportExpressions() {
        List<CategoryExpressionAsStringsDTO> categoryExpressionAsStringsDTOS = new LinkedList<>();

        List<String> categoryNames = new LinkedList<>();
        List<String> expressionNames1 = new LinkedList<>();
        List<String> expressionNames2 = new LinkedList<>();

        for(int i = 1; i < 3; i++) {
            categoryNames.add("Category " + i);
        }

        for(int i = 1; i < 6; i++) {
            expressionNames1.add("Expression " + i);
        }

        for(int i = 1; i < 6; i++) {
            expressionNames2.add("Term " + i);
        }

        categoryExpressionAsStringsDTOS.add(new CategoryExpressionAsStringsDTO(categoryNames.get(0), expressionNames1));
        categoryExpressionAsStringsDTOS.add(new CategoryExpressionAsStringsDTO(categoryNames.get(1), expressionNames2));

        ResponseEntity<List<CategoryExpressionDTO>> response = this.expressionController.importExpressions(categoryExpressionAsStringsDTOS);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(5, this.categoryService.getAllCategories().size());
        Assertions.assertEquals(30, this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryById(0L)).size());
        Assertions.assertEquals(5, this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryById(11L)).size());
        Assertions.assertEquals(5, this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryById(12L)).size());
    }
}

