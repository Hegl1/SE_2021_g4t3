package at.qe.timeguess.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class User {

	@Id
	@GeneratedValue
	private Long id;
	private String username;
	@JsonIgnore
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	public User() {
	}

	public User(final String username, final String password, final UserRole role) {
		this.username = username;
		this.password = password;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public UserRole getRole() {
		return role;
	}

	public String getPassword() {
		return password;
	}

	public Long getId() {
		return id;
	}
}
