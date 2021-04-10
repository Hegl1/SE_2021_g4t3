package at.qe.timeguess.services;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;
import at.qe.timeguess.repositories.ExpressionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Scope("application")
public class ExpressionService {

    @Autowired
    private ExpressionRepository expressionRepository;

    public Expression getExpressionById(final Long id) {
        return this.expressionRepository.findFirstById(id);
    }

    public Expression getExpressionByCategoryAndName(final Category category, final String name) {
        return this.expressionRepository.findFirstByCategoryAndName(category, name);
    }

    public Collection<Expression> getAllExpressions() {
        return this.expressionRepository.findAll();
    }

    public Collection<Expression> getAllExpressionsByCategory(final Category category) {
        return this.expressionRepository.findByCategory(category);
    }

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
    public class ExpressionAlreadyExists extends Exception {

        private static final long serialVersionUID = 1L;

        public ExpressionAlreadyExists(final String message) {
            super(message);
        }
    }
}
