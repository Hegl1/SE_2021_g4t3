package at.qe.timeguess.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    @JsonIgnore
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
