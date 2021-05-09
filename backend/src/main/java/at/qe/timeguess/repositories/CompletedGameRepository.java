package at.qe.timeguess.repositories;

import java.util.Date;
import java.util.List;

import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.CompletedGame;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompletedGameRepository extends AbstractRepository<CompletedGame, Long> {

	List<CompletedGame> findByStartTime(Date startTime);

	List<CompletedGame> findByCategory(Category category);

	@Query(value = "SELECT * FROM completed_game cg " +
        "JOIN completed_game_attended_teams cgat ON cg.id = cgat.completed_game_id " +
        "JOIN completed_game_team cgt ON cgat.attended_teams_id = cgt.id " +
        "JOIN completed_game_team_players cgtp ON cgtp.completed_game_team_id = cgt.id " +
        "WHERE cgtp.players_id = :userId AND cgt.has_won = :hasWon", nativeQuery = true)
    List<CompletedGame> findWonByUserId(@Param("userId") Long userId, @Param("hasWon") boolean hasWon);

	@Query(value = "SELECT COUNT(*) " +
        "FROM completed_game cg " +
        "    JOIN completed_game_attended_teams cgat ON cg.id = cgat.completed_game_id " +
        "    JOIN completed_game_team cgt ON cgat.attended_teams_id = cgt.id " +
        "    JOIN completed_game_team_players cgtp ON cgtp.completed_game_team_id = cgt.id " +
        "WHERE cgtp.players_id = :userId AND cgt.has_won = :hasWon AND cg.category_id = :categoryId", nativeQuery = true)
    Integer getAmountWonByUserIdForCategoryId(@Param("userId") Long userId, @Param("hasWon") boolean hasWon, @Param("categoryId") Long categoryId);
}
