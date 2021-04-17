package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.NameDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.services.CategoryService;
import at.qe.timeguess.services.ExpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that controls the managing of the Expressions
 *
 */
@RequestMapping("")
@RestController
public class ExpressionController {

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Returns a List of all Expressions in a Category
     *
     * @param categoryId the ID of the Category of which the Expressions are asked for
     * @return ResponseEntity for REST communication:
     *      code OK         if the Expressions got found
     *      code NOT_FOUND  if there are no Expressions
     */
    @GetMapping("/categories/{id}/expressions")
    public ResponseEntity<List<Expression>> getAllExpressionsOfCategory(final Long categoryId){
        Category category = this.categoryService.getCategoryById(categoryId);
        List<Expression> allExpressions = new LinkedList<>(this.expressionService.getAllExpressionsByCategory(category));

        if(allExpressions.size() > 0) {
            return new ResponseEntity<>(allExpressions, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Creates and saves a new Expression into the database
     *
     * @param categoryId the ID of the Category to which the Expression is assigned
     * @param nameDTO contains name of the Expression to be created
     * @return ResponseEntity for REST communication:
     *      code OK         if Expression got created successfully
     *      code NOT_FOUND  if Category to assign is not found
     *      code CONFLICT   if Expression already exists
     */
    @PostMapping("/categories/{id}/expressions")
    public ResponseEntity<Expression> createExpression(final Long categoryId, @RequestBody final NameDTO nameDTO){
        Category category = this.categoryService.getCategoryById(categoryId);
        Expression expression = new Expression(nameDTO.getName(), category);

        if(category != null) {
            try {
                this.expressionService.saveExpression(expression);
                return new ResponseEntity<>(expression, HttpStatus.OK);
            } catch (ExpressionService.ExpressionAlreadyExists e) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes an existing Expression
     *
     * @param id the ID of the Category
     * @return ResponseEntity for REST communication:
     *      code OK         if Expression got deleted successfully
     *      code NOT_FOUND  if Expression to delete was not found
     */
    @DeleteMapping("expressions/{id}")
    public ResponseEntity<Expression> deleteExpression(final Long id) {
        Expression expression = this.expressionService.getExpressionById(id);
        try {
            this.expressionService.deleteExpression(expression);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ExpressionService.ExpressionDoesNotExistAnymore e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Imports several Expressions and assigns them to one specific Category
     *
     * @param categoryId the ID of the Category to which the Expressions are assigned to
     * @param expressionNames the List of names of the Expressions
     * @return ResponseEntity for REST communication (
     *      code 200 if Expressions got imported successfully)
     */
    @PostMapping("categories/{id}/expressions/import")
    public ResponseEntity<List<Expression>> importExpressionsIntoCategory(final Long categoryId, final List<String> expressionNames) {
        List<Expression> importedExpressions = new LinkedList<>(this.expressionService.importExpressionsIntoCategory(categoryId, expressionNames));
        return new ResponseEntity<>(importedExpressions, HttpStatus.CREATED);
    }
}
