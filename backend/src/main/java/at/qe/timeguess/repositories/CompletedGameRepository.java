package at.qe.timeguess.repositories;

import java.util.Date;
import java.util.List;

import at.qe.timeguess.model.CompletedGame;

public interface CompletedGameRepository extends AbstractRepository<CompletedGame, Long> {

	CompletedGame findFirstByid(Long id);

	List<CompletedGame> findByStartTime(Date startTime);

}
