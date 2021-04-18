package at.qe.timeguess.repositories;

import at.qe.timeguess.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends AbstractRepository<User, Long> {

	User findFirstByUsername(String username);

    @Query("SELECT u.username FROM User u WHERE  lower(u.username) LIKE %:username%")
	List<String> searchByUsername(@Param("username") String searchString);

}
