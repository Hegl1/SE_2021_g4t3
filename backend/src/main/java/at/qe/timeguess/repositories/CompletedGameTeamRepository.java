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

    @Query("SELECT CompletedGame.id, end_time, start_time, category_id " +
        "FROM CompletedGameTeam " +
        "JOIN completed_game_attended_teams ON CompletedGameTeam.id = completed_game_attended_teams.attended_teams_id " +
        "JOIN CompletedGame                 ON completed_game_attended_teams.completed_game_id = CompletedGame.id " +
        "WHERE :user MEMBER OF CompletedGameTeam.players")
    public List<CompletedGame> findCompletedGames(@Param("user") User user);

}
