package at.qe.timeguess.api.service;

import at.qe.timeguess.api.model.Team;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TeamService {

    private static final AtomicLong ID_COUNTER = new AtomicLong();
    private static final ConcurrentHashMap<Long,Team> teams = new ConcurrentHashMap<>();

    public Team addTeam(Team team) {
        Team newTeam = new Team();
        newTeam.setId(team.getId());
        newTeam.setName(team.getName());
        newTeam.setPoints((team.getPoints() == null) ? 0 : team.getPoints());
        teams.put(newTeam.getId(), newTeam);
        return newTeam;
    }

    public Team findOneTeam(Long id) {
        return teams.get(id);
    }

    public Team updateTeam(Long id, Team team) {
        if(team.getName() != null) {
            teams.computeIfPresent(id,(key,value) -> {
                value.setName(team.getName());
                return value;
            });

            if(team.getPoints() != null) {
                teams.computeIfPresent(id,(key,value) -> {
                    value.setPoints(team.getPoints());
                    return value;
                });
            }
        }

        return teams.get(id);
    }
}
