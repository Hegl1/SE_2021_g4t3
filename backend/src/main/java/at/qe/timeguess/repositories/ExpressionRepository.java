package at.qe.timeguess.repositories;

import java.util.List;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.Expression;

public interface ExpressionRepository extends AbstractRepository<Expression, Long> {

	Expression findFirstByCategoryAndName(Category category, String name);

	List<Expression> findByCategory(Category category);

	List<Expression> findByName(String name);

}
