package at.qe.timeguess.api.controllers;

import at.qe.timeguess.api.model.Team;
import at.qe.timeguess.api.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TeamController {

    @Autowired
    TeamService teamService;

    @PostMapping("/teams")
    private Team createTeam(@RequestBody Team team) {
        return teamService.addTeam(team);
    }
    @GetMapping("/teams/{id}")
    private Team getOneTeam(@PathVariable Long id) {
        return teamService.findOneTeam(id);
    }

    @PatchMapping("/teams/{id}")
    private Team updateTeam(@PathVariable Long id, @RequestBody Team team) {
        return teamService.updateTeam(id,team);
    }
}
