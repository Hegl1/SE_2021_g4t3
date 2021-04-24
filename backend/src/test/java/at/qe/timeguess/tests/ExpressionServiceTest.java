package at.qe.timeguess.tests;

import at.qe.timeguess.dto.CategoryExpressionAsStringsDTO;
import at.qe.timeguess.dto.NameDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.services.CategoryService;
import at.qe.timeguess.services.ExpressionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootTest
public class ExpressionServiceTest {

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private CategoryService categoryService;
    private Object ArrayList;

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
        Assertions.assertEquals(5, allExpressions.size());
        Assertions.assertTrue(allExpressions.stream().anyMatch(expression -> expression.getName().equals("Bundestag")));
    }

    @Test
    public void testGetAllExpressionsByCategory() {
        Category category = this.categoryService.getCategoryById(0L);
        Collection<Expression> allExpressionsOfCategory = this.expressionService.getAllExpressionsByCategory(category);
        Assertions.assertEquals(5, allExpressionsOfCategory.size());
        Assertions.assertTrue(allExpressionsOfCategory.stream().anyMatch(expression -> expression.getName().equals("Bundestag")));
    }

    @Test
    public void testIsDeletable() throws CategoryService.CategoryAlreadyExistsException {
        Category notDeletableCategory = this.categoryService.getCategoryById(0L);
        Category deletableCategory = this.categoryService.saveCategory(new Category("Politics"));

        Assertions.assertFalse(this.categoryService.isDeletable(notDeletableCategory));
        Assertions.assertTrue(this.categoryService.isDeletable(deletableCategory));
    }

    @Test
    public void testGetRandomExpressionByCategory() {
        Category category = this.categoryService.getCategoryById(0L);
        Expression randomExpression = this.expressionService.getRandomExpressionByCategory(category);
        Collection<Expression> allExpressionsByCategory = this.expressionService.getAllExpressionsByCategory(category);

        Assertions.assertTrue(allExpressionsByCategory.stream().anyMatch(expression -> expression.getName().equals(randomExpression.getName())));
    }

    @Test
    @DirtiesContext
    public void testSaveExpression() throws ExpressionService.ExpressionAlreadyExists {

        NameDTO nameDTO = new NameDTO("Ballermann");
        this.expressionService.saveExpression(0L, nameDTO);
        Collection<Expression> allExpressionsOfCategory = this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryById(0L));

        Assertions.assertEquals(6, allExpressionsOfCategory.size());
        Assertions.assertTrue(allExpressionsOfCategory.stream().anyMatch(expression -> expression.getName().equals("Ballermann")));
    }

    @Test
    @DirtiesContext
    public void testImportExpressionsByCategory() throws ExpressionService.ExpressionAlreadyExists {
        Collection<String> expressionNamesToImport = new ArrayList<>();

        for(int i = 1; i < 6; i++) {
            expressionNamesToImport.add("Expression " + i);
        }
        this.expressionService.importExpressionsIntoCategory(0L, expressionNamesToImport);

        Category category = this.categoryService.getCategoryByName("Deutschland");
        Collection<Expression> allExpressionsOfCategory = this.expressionService.getAllExpressionsByCategory(category);
        Assertions.assertEquals(10, allExpressionsOfCategory.size());
    }

    @Test
    @DirtiesContext
    void testImportExpressions() throws CategoryService.CategoryAlreadyExistsException, ExpressionService.ExpressionAlreadyExists {
        Collection<CategoryExpressionAsStringsDTO> categoryExpressionAsStringsDTOS = new ArrayList<>();

        List<String> expressionNames = new ArrayList<>();

        for(int i = 1; i < 6; i++) {
            expressionNames.add("Expression " + i);
        }

        CategoryExpressionAsStringsDTO categoryExpressionAsStringsDTO = new CategoryExpressionAsStringsDTO("Deutschland", expressionNames);
        categoryExpressionAsStringsDTOS.add(categoryExpressionAsStringsDTO);
        this.expressionService.importExpressions(categoryExpressionAsStringsDTOS);

        Assertions.assertEquals(1, this.categoryService.getAllCategories().size());
        Assertions.assertEquals(10, this.expressionService.getAllExpressionsByCategory(this.categoryService.getCategoryByName("Deutschland")).size());
    }

    @Test
    @DirtiesContext
    public void testDeleteExpression() throws ExpressionService.ExpressionAlreadyExists, ExpressionService.ExpressionDoesNotExistAnymore {

        // TODO: can't delete predefined test data
        NameDTO nameDTO = new NameDTO("Ballermann");
        this.expressionService.saveExpression(0L, nameDTO);
        this.expressionService.deleteExpression(this.expressionService.getExpressionById(1L));
        Assertions.assertEquals(5, this.expressionService.getAllExpressions().size());
    }

    @Test
    @DirtiesContext
    public void testExpressionAlreadyExistsException() {
        NameDTO nameDTO = new NameDTO("Bundestag");
        Assertions.assertThrows(ExpressionService.ExpressionAlreadyExists.class, () -> this.expressionService.saveExpression(0L, nameDTO));
    }
}
