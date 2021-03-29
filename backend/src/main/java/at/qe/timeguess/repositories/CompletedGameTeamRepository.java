package at.qe.timeguess.repositories;

import at.qe.timeguess.model.CompletedGameTeam;

public interface CompletedGameTeamRepository extends AbstractRepository<CompletedGameTeam, Long> {

	CompletedGameTeam findFirstByid(Long id);
}
