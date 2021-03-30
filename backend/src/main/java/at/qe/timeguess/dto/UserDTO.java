package at.qe.timeguess.dto;

/**
 * Class used to send users for games and statistics via rest. Not possible to
 * do with entity because password would be send with.
 *
 */
public class UserDTO {

	private Long id;
	private String username;
	private String role;

	public UserDTO(final long id, final String username, final String role) {
		super();
		this.id = id;
		this.username = username;
		this.role = role;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(final String role) {
		this.role = role;
	}

}
