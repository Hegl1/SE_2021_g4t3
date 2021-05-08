package at.qe.timeguess.model;

import javax.persistence.*;

@Entity
public class Category {

    // TODO: add deletable attribute?
	@Id
    @SequenceGenerator(name = "category_sequence", initialValue = 11)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_sequence")
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
