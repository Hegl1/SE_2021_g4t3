package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.CategoryExpressionAsStringsDTO;
import at.qe.timeguess.dto.CategoryExpressionDTO;
import at.qe.timeguess.dto.ExpressionDTO;
import at.qe.timeguess.dto.NameDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.services.CategoryService;
import at.qe.timeguess.services.ExpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/categories/{categoryId}/expressions")
    public ResponseEntity<List<ExpressionDTO>> getAllExpressionsOfCategory(@PathVariable final Long categoryId) {
        Category category = this.categoryService.getCategoryById(categoryId);

        if(category != null) {

            List<Expression> allExpressions = new LinkedList<>(this.expressionService.getAllExpressionsByCategory(category));
            List<ExpressionDTO> allExpressionDTOs = new LinkedList<>();
            for(Expression current : allExpressions) {
                allExpressionDTOs.add(new ExpressionDTO(current.getId(), current.getName()));
            }
            return new ResponseEntity<>(allExpressionDTOs, HttpStatus.OK);
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
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('GAMEMANAGER')")
    @PostMapping("/categories/{categoryId}/expressions")
    public ResponseEntity<ExpressionDTO> createExpression(@PathVariable final Long categoryId, @RequestBody final NameDTO nameDTO) {
        Category category = this.categoryService.getCategoryById(categoryId);

        if(category != null) {
            try {
                ExpressionDTO expressionDTO = this.expressionService.saveExpression(categoryId, nameDTO);
                return new ResponseEntity<>(expressionDTO, HttpStatus.OK);
            } catch (ExpressionService.ExpressionAlreadyExistsException e) {
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
     *      code CONFLICT   if Expression to delete is referenced in a Game
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('GAMEMANAGER')")
    @DeleteMapping("/expressions/{id}")
    public ResponseEntity<Expression> deleteExpression(@PathVariable final Long id) {
        Expression expression = this.expressionService.getExpressionById(id);

        try {
            this.expressionService.deleteExpression(expression);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ExpressionService.ExpressionDoesNotExistAnymoreException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Imports several Expressions and assigns them to one specific Category
     *
     * @param categoryId the ID of the Category to which the Expressions are assigned to
     * @param expressionNames the List of names of the Expressions
     * @return ResponseEntity for REST communication:
     *      code CREATED if Expressions got imported successfully
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('GAMEMANAGER')")
    @PostMapping("/categories/{categoryId}/expressions/import")
    public ResponseEntity<List<ExpressionDTO>> importExpressionsIntoCategory(@PathVariable final Long categoryId, @RequestBody final List<String> expressionNames) {
        List<ExpressionDTO> expressionDTOs = null;
        expressionDTOs = this.expressionService.importExpressionsIntoCategory(categoryId, expressionNames);

        return new ResponseEntity<>(expressionDTOs, HttpStatus.CREATED);
    }

    /**
     * Imports several Expressions and assigns them to different Categories
     *
     * @param categoryExpressionAsStringsDTOS contains the Categories and Expressions to import
     * @return ResponseEntity for REST communication:
     *      code CREATED if Categories and Expressions got imported successfully
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('GAMEMANAGER')")
    @PostMapping("/expressions/import")
    public ResponseEntity<List<CategoryExpressionDTO>> importExpressions(@RequestBody final List<CategoryExpressionAsStringsDTO> categoryExpressionAsStringsDTOS) {
        List<CategoryExpressionDTO> importedCategoriesAndExpressions = new LinkedList<>();
        importedCategoriesAndExpressions.addAll(this.expressionService.importExpressions(categoryExpressionAsStringsDTOS));

        return new ResponseEntity<>(importedCategoriesAndExpressions, HttpStatus.CREATED);
    }
}
