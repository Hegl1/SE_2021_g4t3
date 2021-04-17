package at.qe.timeguess.services;

import at.qe.timeguess.dto.CategoryExpressionDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.repositories.ExpressionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents the Service with which Expressions get managed.
 * It is possible to create, import with JSON file, delete Expressions,
 * and to find Expression by specific properties as well.
 *
 */
@Service
@Scope("application")
public class ExpressionService {

    // TODO: add PreAuthorize-annotations where needed

    @Autowired
    private ExpressionRepository expressionRepository;

    @Autowired
    private CategoryService categoryService;

    /**
     * Returns a Expression which is searched by an ID
     *
     * @param id the ID with which the expression get searched by
     * @return the expression with the corresponding ID
     */
    public Expression getExpressionById(final Long id) {
        return this.expressionRepository.findFirstById(id);
    }

    /**
     * Returns a Expression which is searched by its Category and name
     *
     * @param category the Category in which the Expression is in
     * @param name the name of the Expression
     * @return the Expression with corresponding name and Category
     */
    public Expression getExpressionByCategoryAndName(final Category category, final String name) {
        return this.expressionRepository.findFirstByCategoryAndName(category, name);
    }

    /**
     * Returns all Expressions in the database
     *
     * @return all Expressions
     */
    public Collection<Expression> getAllExpressions() {
        return this.expressionRepository.findAll();
    }

    /**
     * Returns all Expressions of a given Category
     *
     * @param category the Category of which all the Expressions are searched for
     * @return all the Expressions of a given Category
     */
    public Collection<Expression> getAllExpressionsByCategory(final Category category) {
        return this.expressionRepository.findByCategory(category);
    }

    /**
     * Saves an Expression in the database
     *
     * @param expression the Expression to be saved
     * @return the saved Expression
     * @throws ExpressionAlreadyExists if the Expression already exists in the Category
     */
    public Expression saveExpression(final Expression expression) throws ExpressionAlreadyExists {

        Collection<Expression> allExpressionsOfCategory = getAllExpressionsByCategory(expression.getCategory());

        if(allExpressionsOfCategory.stream().anyMatch(e -> e.getName().equals(expression.getName()))) {
            throw new ExpressionAlreadyExists("This Expression already exists in this Category!");
        }
        return this.expressionRepository.save(expression);
    }

    /**
     *  Imports Expressions into the database and assigns them to a given Category
     *
     * @param categoryName the name of the Category to which the Expressions get assigned
     * @param expressionNames the names of the Expressions to be imported
     * @return the Collection of the imported Expressions
     */
    public Collection<Expression> importExpressionsIntoCategory(final String categoryName, final Collection<String> expressionNames) {

        Category categoryToImportTo = this.categoryService.getCategoryByName(categoryName);
        Expression expressionToImport;
        Collection<Expression> expressionsToImport = new ArrayList<>();

        for(String current : expressionNames) {
            expressionToImport = new Expression(current, categoryToImportTo);
            expressionsToImport.add(expressionToImport);
            try {
                saveExpression(expressionToImport);
            } catch (ExpressionAlreadyExists expressionAlreadyExists) {
                expressionAlreadyExists.printStackTrace();
            }
        }

        return expressionsToImport;
    }

    /**
     *  Imports Expressions of multiple Categories and creates Categories if required Category does not exist already
     *
     * @param categoryExpressionDTOs Pairs of (Category, Expression[]) to get imported
     * @throws CategoryService.CategoryAlreadyExistsException if the Category to get created already exists
     */
    public void importExpressions(final Collection<CategoryExpressionDTO> categoryExpressionDTOs) throws CategoryService.CategoryAlreadyExistsException {
        String nameOfCategoryToImportTo;

        for(CategoryExpressionDTO current : categoryExpressionDTOs) {
            nameOfCategoryToImportTo = current.getCategoryName();

            if (this.categoryService.getCategoryByName(nameOfCategoryToImportTo) == null) {
                Category newCategoryToSave = new Category(nameOfCategoryToImportTo);
                this.categoryService.saveCategory(newCategoryToSave);
            }

            this.importExpressionsIntoCategory(nameOfCategoryToImportTo, current.getExpressionNames());
        }
    }

    public void deleteExpression(final Expression expression) throws ExpressionDoesNotExistAnymore {
        if(expression == null) {
            throw new ExpressionDoesNotExistAnymore("This Expression does not exist anymore!");
        } else {
            this.expressionRepository.delete(expression);
        }
    }

    /**
     * Gets thrown when an Expression is tried to be created, which already exists
     */
    public class ExpressionAlreadyExists extends Exception {

        private static final long serialVersionUID = 1L;

        public ExpressionAlreadyExists(final String message) {
            super(message);
        }
    }

    /**
     * Gets thrown when an Expression is tried to be deleted, which does not exist anymore
     */
    public class ExpressionDoesNotExistAnymore extends Exception {

        private static final long serialVersionUID = 1L;

        public ExpressionDoesNotExistAnymore(final String message) {
            super(message);
        }
    }
}
