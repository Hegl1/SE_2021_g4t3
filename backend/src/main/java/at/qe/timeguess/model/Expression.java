package at.qe.timeguess.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Expression {

	@Id
	@GeneratedValue
	private long iD;
	private String expression;

	@OneToOne
	private Category category;

	public Expression() {

	}

	public Expression(final String expression, final Category category) {
		this.expression = expression;
		this.category = category;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(final String expression) {
		this.expression = expression;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(final Category category) {
		this.category = category;
	}

}
