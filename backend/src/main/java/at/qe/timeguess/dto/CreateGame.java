package at.qe.timeguess.dto;

/**
 * Class used to send receive information about a new game via rest.
 *
 */
public class CreateGame {

	private String dice_code;

	private int category_id;

	private Mapping[] mapping;

	private int number_of_teams;

	private int max_score;

	public CreateGame(final String dice_code, final int category_id, final Mapping[] mapping, final int number_of_teams,
			final int max_score) {
		super();
		this.dice_code = dice_code;
		this.category_id = category_id;
		this.mapping = mapping;
		this.number_of_teams = number_of_teams;
		this.max_score = max_score;
	}

	public String getDice_code() {
		return dice_code;
	}

	public int getMax_score() {
		return max_score;
	}

	public void setMax_score(final int max_score) {
		this.max_score = max_score;
	}

	public int getCategory_id() {
		return category_id;
	}

	public Mapping[] getMapping() {
		return mapping;
	}

	public int getNumber_of_teams() {
		return number_of_teams;
	}

}
