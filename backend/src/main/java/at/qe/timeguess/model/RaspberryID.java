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
	private String identifier;

	public RaspberryID(final String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

}
