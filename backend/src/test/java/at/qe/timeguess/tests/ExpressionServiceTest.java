package at.qe.timeguess.tests;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.services.CategoryService;
import at.qe.timeguess.services.ExpressionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;

@SpringBootTest
public class ExpressionServiceTest {

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testGetExpressionById() {
        Expression expression = this.expressionService.getExpressionById(0L);
        Assertions.assertEquals("Bundestag", expression.getName());
    }

    @Test
    public void testGetExpressionByCategoryAndName() {
        Category category = this.categoryService.getCategoryByName("Deutschland");
        Expression expression = this.expressionService.getExpressionByCategoryAndName(category, "Bundestag");
        Assertions.assertEquals(0L, expression.getId());
    }

    @Test
    public void testGetAllExpressions() {
        Collection<Expression> allExpressions = this.expressionService.getAllExpressions();
        Assertions.assertEquals(1, allExpressions.size());
        Assertions.assertTrue(allExpressions.stream().anyMatch(expression -> expression.getName().equals("Bundestag")));
    }

    @Test
    public void testGetAllExpressionsByCategory() {
        Category category = this.categoryService.getCategoryById(0L);
        Collection<Expression> allExpressionsOfCategory = this.expressionService.getAllExpressionsByCategory(category);
        Assertions.assertEquals(1, allExpressionsOfCategory.size());
        Assertions.assertTrue(allExpressionsOfCategory.stream().anyMatch(expression -> expression.getName().equals("Bundestag")));
    }

    @Test
    @DirtiesContext
    public void testSaveExpression() throws ExpressionService.ExpressionAlreadyExists {
        Category category = this.categoryService.getCategoryById(0L);
        Expression expressionToSave = new Expression("Berlin", category);
        this.expressionService.saveExpression(expressionToSave);

        Collection<Expression> allExpressionsOfCategory = this.expressionService.getAllExpressionsByCategory(category);
        Assertions.assertEquals(2, allExpressionsOfCategory.size());
        Assertions.assertTrue(allExpressionsOfCategory.stream().anyMatch(expression -> expression.getName().equals("Berlin")));
    }
/*
    @Test
    public void testImportExpressions() {

    }

    @Test
    @DirtiesContext
    public void testDeleteExpression() {
        Expression expressionToDelete = this.expressionService.getExpressionById(0L);
        this.expressionService.deleteExpression(expressionToDelete);
        Assertions.assertEquals(0, this.expressionService.getAllExpressions().size());
    }
*/
    @Test
    @DirtiesContext
    public void testExpressionAlreadyExistsException() throws ExpressionService.ExpressionAlreadyExists {
        Category category = this.categoryService.getCategoryById(0L);
        Expression expressionToSave = new Expression("Bundestag", category);
        Assertions.assertThrows(ExpressionService.ExpressionAlreadyExists.class, () -> this.expressionService.saveExpression(expressionToSave));
    }
}
