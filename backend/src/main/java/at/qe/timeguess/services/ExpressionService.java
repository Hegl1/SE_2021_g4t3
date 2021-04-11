package at.qe.timeguess.services;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.repositories.ExpressionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
/*
    public Expression importExpressions() {

        // get JSON file

        // read out Category

        // create and save Category if it doesn't exist already

        // read out Expressions

        // save all Expressions
    }

    public void deleteExpression(final Expression expression) {
        this.expressionRepository.delete(expression);
    }
*/

    /**
     * Gets thrown when an Expression is tried to be created, which already exists
     */
    public class ExpressionAlreadyExists extends Exception {

        private static final long serialVersionUID = 1L;

        public ExpressionAlreadyExists(final String message) {
            super(message);
        }
    }
}
