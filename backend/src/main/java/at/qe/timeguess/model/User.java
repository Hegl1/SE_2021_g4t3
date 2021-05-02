package at.qe.timeguess.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Objects;

@Entity
public class User {

    @Id
    @SequenceGenerator(name = "user_sequence", initialValue = 21)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequence")
    private Long id;
    private String username;
    @JsonIgnore
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date updateDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public User() {
    }

    public User(final String username, final String password, final UserRole role) {
        this.username = username;
        this.password = encode(password);
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
        this.password = encode(password);
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * if two user have the same username, password, role and updateDate they are equal
     * @param o user to compare
     * @return boolean that states if users are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        boolean sameUpdateDate = false;
        //because repository returns Timestamp object and User objects return Date objects we need to compare the long timestamps of the dates
        if (updateDate != null && user.updateDate != null) {
            sameUpdateDate = Objects.equals(updateDate.getTime(), user.updateDate.getTime());
        } else if(updateDate == null && user.updateDate == null){
            sameUpdateDate = true;
        }
        return Objects.equals(id, user.id) && Objects.equals(username, user.username) && role == user.role && sameUpdateDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, updateDate, role);
    }

    public String encode(String password) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        if (password != null && !password.isEmpty())  {
            return encoder.encode(password);
        }

        return null;
    }
}
