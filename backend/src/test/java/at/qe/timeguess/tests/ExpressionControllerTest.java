package at.qe.timeguess.tests;

import at.qe.timeguess.controllers.ExpressionController;
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
import org.springframework.test.annotation.DirtiesContext;

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
    public void testCreateExpressionHttpStatusOk() {
        NameDTO nameDTO = new NameDTO("Stefan Raab");
        ResponseEntity<ExpressionDTO> response = this.expressionController.createExpression(0L, nameDTO);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Stefan Raab", response.getBody().getName());
        Assertions.assertEquals(6, this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryById(0L)).size());
    }

    @Test
    @DirtiesContext
    public void testCreateExpressionHttpStatusNotFound() {
        NameDTO nameDTO = new NameDTO("Stefan Raab");
        ResponseEntity<ExpressionDTO> response = this.expressionController.createExpression(5L, nameDTO);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testCreateExpressionHttpStatusConflict() {
        NameDTO nameDTO = new NameDTO("Bundestag");
        ResponseEntity<ExpressionDTO> response = this.expressionController.createExpression(0L, nameDTO);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testDeleteExpressionHttpStatusOk() throws ExpressionService.ExpressionAlreadyExists, CategoryService.CategoryAlreadyExistsException {
        Category category = this.categoryService.saveCategory(new Category("Politics"));
        NameDTO nameDTO = new NameDTO("Trump");
        this.expressionService.saveExpression(11L, nameDTO);
        ResponseEntity<Expression> response = this.expressionController.deleteExpression(11L);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(0, this.expressionService.getAllExpressionsByCategory(category));
    }

    @Test
    @DirtiesContext
    public void testDeleteExpressionHttpStatusNotFound() {
        ResponseEntity<Expression> response = this.expressionController.deleteExpression(500L);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testDeleteExpressionHttpStatusConflict() {
        ResponseEntity<Expression> response = this.expressionController.deleteExpression(1L);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    public void testImportExpressionsIntoCategory() {

    }

    @Test
    @DirtiesContext
    public void testImportExpressions() {

    }
}
