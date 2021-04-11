package at.qe.timeguess.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Category {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String name;

	public Category() {
	}

	public Long getId() {
		return id;
	}

	public Category(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
