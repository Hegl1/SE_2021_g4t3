package at.qe.timeguess.repositories;

import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.model.CompletedGameTeam;
import at.qe.timeguess.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompletedGameTeamRepository extends AbstractRepository<CompletedGameTeam, Long> {

    @Query("Select t FROM CompletedGameTeam t WHERE :user MEMBER OF t.players")
    public List<CompletedGameTeam> findByUser(@Param("user") User user);

}
