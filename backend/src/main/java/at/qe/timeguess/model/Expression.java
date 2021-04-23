package at.qe.timeguess.model;

import javax.persistence.*;

@Entity
public class Expression {

	@Id
    @SequenceGenerator(name = "expression_sequence", initialValue = 11)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expression_sequence")
	private long id;
	private String name;

	@OneToOne
	private Category category;

	public Expression() {

	}

	public Expression(final String name, final Category category) {
		this.name = name;
		this.category = category;
	}

	public Long getId() {
	    return this.id;
    }

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(final Category category) {
		this.category = category;
	}

}
