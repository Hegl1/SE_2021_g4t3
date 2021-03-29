package at.qe.timeguess.repositories;

import at.qe.timeguess.model.User;

public interface UserRepository extends AbstractRepository<User, Long> {

	User findFirstByUsername(String username);

	User findFirstByid(Long id);

}
