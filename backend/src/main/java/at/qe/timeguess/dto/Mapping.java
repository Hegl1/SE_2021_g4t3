package at.qe.timeguess.dto;

/**
 * Class to recieve dice mappings as part of the {@link CreateGame} class.
 *
 */
public class Mapping {

	private String action;
	private int time;
	private int points;

	public Mapping(final String action, final int time, final int points) {
		super();
		this.action = action;
		this.time = time;
		this.points = points;
	}

	public String getAction() {
		return action;
	}

	public int getTime() {
		return time;
	}

	public int getPoints() {
		return points;
	}
}
