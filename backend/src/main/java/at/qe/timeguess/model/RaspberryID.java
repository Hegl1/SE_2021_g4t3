package at.qe.timeguess.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Class for persisting the identifiers of already known raspberries.
 *
 */

@Entity
public class RaspberryID {

	@Id
	private String id;

	public RaspberryID() {
	}

	public RaspberryID(final String identifier) {
		this.id = identifier;
	}

	public String getIdentifier() {
		return id;
	}

	public void setIdentifier(final String identifier) {
		this.id = identifier;
	}

}
