package at.qe.timeguess.repositories;

import java.util.List;

import at.qe.timeguess.model.Expression;

public interface ExpressionRepository extends AbstractRepository<Expression, Long> {

	Expression findFirstByCategoryAndExpression(String category, String expression);

	List<Expression> findByCategory(String category);

	List<Expression> findByExpression(String expression);

}
