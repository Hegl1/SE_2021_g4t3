package at.qe.timeguess.services;

import at.qe.timeguess.dto.*;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.model.CompletedGameTeam;
import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.CompletedGameRepository;
import at.qe.timeguess.repositories.CompletedGameTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("application")
public class StatisticsService {

    @Autowired
    private CompletedGameRepository completedGameRepository;

    @Autowired
    private CompletedGameTeamRepository completedGameTeamRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    public CompletedGame buildCompletedGame(final Date startTime, final Date endTime, final Category category,
                                            final Collection<CompletedGameTeam> teams) {

        return new CompletedGame(startTime, endTime, category, teams);
    }

    public CompletedGame persistCompletedGame(final Date startTime, final Date endTime, final Category category,
                                              final Collection<CompletedGameTeam> teams) {

        CompletedGame completedGame = buildCompletedGame(startTime, endTime, category, teams);
        this.completedGameRepository.save(completedGame);

        for(CompletedGameTeam current : teams) {
            this.persistCompletedGameTeam(current);
        }

        return completedGame;
    }

    public CompletedGameTeam persistCompletedGameTeam(final CompletedGameTeam completedGameTeam) {
        return this.completedGameTeamRepository.save(completedGameTeam);
    }

    public UserStatisticsDTO getUserStatistics(final Long userId) {

        User user = this.userService.getUserById(userId);
        List<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();
        List<Category> allCategories = new LinkedList<>(this.categoryService.getAllCategories());

        List<GameStatisticsDTO> won_games = new LinkedList<>();
        List<GameStatisticsDTO> lost_games = new LinkedList<>();
        Category most_played_category = null;
        int amountMostPlayedCategory = 0;

        for(CompletedGame completedGame : allCompletedGames) {
            for(Category category : allCategories) {
                int wins = 0;
                int losses = 0;

                for(CompletedGameTeam completedGameTeam : completedGame.getAttendedTeams()) {
                    if(completedGameTeam.getPlayers().contains(user)) {
                        if(completedGameTeam.getHasWon()) {
                            wins++;
                        } else {
                            losses++;
                        }
                    }
                }

                if(amountMostPlayedCategory < (wins + losses)) {
                    amountMostPlayedCategory = wins + losses;
                    most_played_category = category;
                }

                won_games.add(new GameStatisticsDTO(category, wins));
                lost_games.add(new GameStatisticsDTO(category, losses));
            }
        }

        int played_games = this.getPlayedGames(user);
        List <UserDTO> played_with = this.getPlayedWith(user);

        return new UserStatisticsDTO(won_games, lost_games, most_played_category, played_games, played_with);
    }

    private int getPlayedGames(User user) {
        List<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();
        List<CompletedGame> allCompletedGamesOfUser = new LinkedList<>();

        for(CompletedGame completedGame : allCompletedGames) {
            for(CompletedGameTeam completedGameTeam : completedGame.getAttendedTeams()) {

                if(completedGameTeam.getPlayers().contains(user)) {
                    allCompletedGamesOfUser.add(completedGame);
                }
            }
        }

        return allCompletedGamesOfUser.size();
    }

    private List<UserDTO> getPlayedWith(User user) {
        List<CompletedGameTeam> completedGamesOfUser = this.completedGameTeamRepository.findByUser(user);
        List<UserDTO> played_with = new LinkedList<>();
        Set<User> distinct_played_with = new HashSet<>();

        for(CompletedGameTeam current : completedGamesOfUser) {
            distinct_played_with.addAll(current.getPlayers());
        }

        for(User current : distinct_played_with) {
            played_with.add(new UserDTO(current.getId(), current.getUsername(), current.getRole().toString()));
        }

        return played_with;
    }

    public GlobalStatisticsDTO getGlobalStatistics() {
        List<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();
        List<Category> allCategories = new LinkedList<>(this.categoryService.getAllCategories());

        int totalGames = allCompletedGames.size();
        int number_correct = 0;
        int number_incorrect = 0;
        Category mostPlayedCategory = null;
        List<User> mostGamesWon = getMostWinningPlayers();

        for(CompletedGame completedGame : allCompletedGames) {
            for(CompletedGameTeam completedGameTeam : completedGame.getAttendedTeams()) {
                number_correct += completedGameTeam.getNumberOfGuessedExpressions();
                number_incorrect += completedGameTeam.getNumberOfWrongExpressions();
            }
        }

        int numberOfGamesOfMostPlayedCategory = 0;
        for(Category category : allCategories) {
            if(allCompletedGames.stream().filter(completedGame -> completedGame.getCategory().getId().equals(category.getId())).count() > numberOfGamesOfMostPlayedCategory) {
                mostPlayedCategory = category;
            }
        }

        return new GlobalStatisticsDTO(totalGames, number_correct, number_incorrect, mostPlayedCategory, mostGamesWon);
    }

    private List<User> getMostWinningPlayers() {
        List<User> mostGamesWon = new LinkedList<>();
        List<User> allUsers = this.userService.getAllUsers();
        int numberOfWonGames = 0;

        for(User user : allUsers) {
            List<CompletedGameTeam> completedGameTeams = this.completedGameTeamRepository.findByUser(user);

            if(completedGameTeams.stream().filter(CompletedGameTeam::getHasWon).count() > numberOfWonGames) {
                mostGamesWon.clear();
                mostGamesWon.add(user);
            } else if(completedGameTeams.stream().filter(CompletedGameTeam::getHasWon).count() == numberOfWonGames) {
                mostGamesWon.add(user);
            }
        }

        return mostGamesWon;
    }

    public CategoryStatisticsDTO getCategoryStatistics() {
        return null;
    }

    public TopGamesStatisticsDTO getTopGamesStatistics() {
        return null;
    }
}
