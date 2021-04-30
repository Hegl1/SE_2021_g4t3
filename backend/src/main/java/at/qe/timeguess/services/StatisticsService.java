package at.qe.timeguess.services;

import at.qe.timeguess.dto.GameStatisticsDTO;
import at.qe.timeguess.dto.UserStatisticsDTO;
import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.model.CompletedGameTeam;
import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.CompletedGameTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@Scope("application")
public class StatisticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private CompletedGameTeamRepository completedGameTeamRepository;

    public UserStatisticsDTO getUserStatistics(final Long UserId) {
        UserStatisticsDTO userStatisticsDTO = new UserStatisticsDTO();

        User user = this.userService.getUserById(UserId);
        List<CompletedGameTeam> completedGameTeamList = this.completedGameTeamRepository.findByUser(user);
        List<CompletedGameTeam> wonCompletedGameTeamList = (List<CompletedGameTeam>) completedGameTeamList.stream().filter(completedGameTeam -> completedGameTeam.getHasWon() == true);
        List<CompletedGameTeam> lostCompletedGameTeamList = (List<CompletedGameTeam>) completedGameTeamList.stream().filter(completedGameTeam -> completedGameTeam.getHasWon() == false);

        List<GameStatisticsDTO> wonGamesDTOs = new LinkedList<>();
        List<GameStatisticsDTO> lostGamesDTOs = new LinkedList<>();

        // List<CompletedGame> completedGamesList = this.completedGameTeamRepository.findCompletedGames(user);

        // System.out.println(completedGamesList);

        return userStatisticsDTO;
    }
}
