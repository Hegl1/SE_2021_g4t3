package at.qe.timeguess.websockDto;

public class JoinOfflineUserDto {

	private String username;
	private String password;

	public JoinOfflineUserDto() {

	}

	public JoinOfflineUserDto(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
