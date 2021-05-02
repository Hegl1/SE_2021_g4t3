package at.qe.timeguess.services;

import at.qe.timeguess.dto.*;
import at.qe.timeguess.gamelogic.Team;
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

/**
 * Class that manges the persisting of completed Games
 * and retrieving Statistics of Games, Players, Categories and more
 * for homepage view
 *
 */
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

    /**
     * Method that builds a new CompletedGame
     *
     * @param startTime the timestamp when the Game started
     * @param endTime the timestamp when the Game ended
     * @param category the Category which was played in the Game
     * @param teams the Teams which played in the Game
     * @return the CompletedGame
     */
    public CompletedGame buildCompletedGame(final Date startTime, final Date endTime, final Category category,
                                            final List<Team> teams) {

        List<CompletedGameTeam> completedGameTeams = buildCompletedGameTeams(teams);
        return new CompletedGame(startTime, endTime, category, completedGameTeams);
    }

    /**
     * Accepts a List of Teams and returns a List of CompletedGameTeams
     * private method, only used in buildCompletedGame and persistCompletedGame methods
     *
     * @param teams the List of Team out of which the List of CompletedGameTeams gets built
     * @return a List of CompletedGameTeams
     */
    private List<CompletedGameTeam> buildCompletedGameTeams(List<Team> teams) {
        List<CompletedGameTeam> completedGameTeams = new LinkedList<>();

        completedGameTeams.add(new CompletedGameTeam(teams.get(0).getNumberOfCorrectExpressions(),
            teams.get(0).getNumberOfWrongExpressions(), teams.get(0).getScore(),
            teams.get(0).getPlayers(), true));

        for(int i = 1; i < teams.size(); i++) {
            completedGameTeams.add(new CompletedGameTeam(teams.get(i).getNumberOfCorrectExpressions(),
                teams.get(i).getNumberOfWrongExpressions(), teams.get(i).getScore(),
                teams.get(i).getPlayers(), false));
        }

        return completedGameTeams;
    }

    /**
     * Method that persists a CompletedGame
     *
     * @param startTime the timestamp when the Game started
     * @param endTime the timestamp when the Game ended
     * @param category the Category which was played in the Game
     * @param teams the Teams which played in the Game
     * @return the CompletedGame
     */
    public CompletedGame persistCompletedGame(final Date startTime, final Date endTime, final Category category,
                                              final List<Team> teams) {

        CompletedGame completedGame = buildCompletedGame(startTime, endTime, category, teams);
        List<CompletedGameTeam> completedGameTeams = this.buildCompletedGameTeams(teams);

        for(CompletedGameTeam current : completedGameTeams) {
            this.completedGameTeamRepository.save(current);
        }

        this.completedGameRepository.save(completedGame);

        return completedGame;
    }

    // TODO: refactor if possible
    /**
     * Method that retrieves Statistics of a User
     *
     * @param userId ID of the User
     * @return UserStatisticsDTO with Statistics of the User
     */
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

    /**
     * Method to get the amount of played Games of a player
     * private method, only used in getUserStatistics method
     *
     * @param user the player of which the amount of played Games is getting retrieved
     * @return the amount of played Games
     */
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

    /**
     * Method that retrieves all players with which a player as played games
     * private method, only used in getUserStatistics method
     *
     * @param user of which the players are getting retrieved
     * @return the list of players
     */
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

    // TODO: refactor
    /**
     * Method that retrieves global Statistics
     *
     * @return a DTO which contains the global Statistics
     */
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
        List<CompletedGame> tempCompletedGame = null;

        for(Category category : allCategories) {

            tempCompletedGame = this.completedGameRepository.findByCategory(category);
            if(tempCompletedGame.size() > numberOfGamesOfMostPlayedCategory) {
                mostPlayedCategory = category;
            }
        }

        return new GlobalStatisticsDTO(totalGames, number_correct, number_incorrect, mostPlayedCategory, mostGamesWon);
    }

    /**
     * Method that retrieves players who have won the most games
     * private method, only used in getGlobalStatistics method
     *
     * @return List of Users which have won the most games
     */
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

    // TODO: refactor
    /**
     * Method that retrieves the Statistics of all Categories
     *
     * @return DTO which contains Statistics of all Categories
     */
    public List<CategoryStatisticsDTO> getCategoryStatistics() {
        List<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();
        List<Category> allCategories = new LinkedList<>(this.categoryService.getAllCategories());

        List<CategoryStatisticsDTO> categoryStatisticsDTOs = new LinkedList<>();
        int number_correct = 0;
        int number_incorrect = 0;

        for(Category category : allCategories) {
            List<CompletedGame> allCompletedGamesOfCategory = this.completedGameRepository.findByCategory(category);
            number_correct = 0;
            number_incorrect = 0;

            for(CompletedGame completedGame : allCompletedGamesOfCategory) {
                for(CompletedGameTeam completedGameTeam : completedGame.getAttendedTeams()) {
                    number_correct += completedGameTeam.getNumberOfGuessedExpressions();
                    number_incorrect += completedGameTeam.getNumberOfWrongExpressions();
                }
            }

            categoryStatisticsDTOs.add(new CategoryStatisticsDTO(category, number_correct, number_incorrect));
        }

        return categoryStatisticsDTOs;
    }

    // TODO: test with more test data
    // TODO: refactor
    /**
     * Method that retrieves the top Games, sorted by score per time
     *
     * @return a List of DTOs which contain Statistics of the top Games
     */
    public List<TopGamesStatisticsDTO> getTopGamesStatistics() {
        List<CompletedGame> allCompletedGames = this.completedGameRepository.findAll();
        PriorityQueue<TopGamesStatisticsDTO> sortedTopGamesStatisticsDTOs = new PriorityQueue<>();

        List<TeamStatisticsDTO> teams = new LinkedList<>();
        Category category = null;
        double score_per_time = 0;
        int duration = 0;

        int score = 0;
        int number_correct = 0;
        int number_incorrect = 0;

        for(CompletedGame completedGame : allCompletedGames) {

            duration = (int) (completedGame.getEndTime().getTime() - completedGame.getStartTime().getTime()) / 1000;

            for(CompletedGameTeam completedGameTeam : completedGame.getAttendedTeams()) {
                score = completedGameTeam.getScore();
                score_per_time = (float) score / duration;
                number_correct = completedGameTeam.getNumberOfGuessedExpressions();
                number_incorrect = completedGameTeam.getNumberOfWrongExpressions();
                category = completedGame.getCategory();

                teams.add(new TeamStatisticsDTO(score, number_correct, number_incorrect));
            }

            sortedTopGamesStatisticsDTOs.add(new TopGamesStatisticsDTO(teams, category, score_per_time, duration));
        }

        List<TopGamesStatisticsDTO> topGames = new LinkedList<>();
        int numberOfTopGames = Integer.min(5, sortedTopGamesStatisticsDTOs.size());

        for(int i = 0; i < numberOfTopGames; i++) {
            topGames.add(sortedTopGamesStatisticsDTOs.poll());
        }

        return topGames;
    }
}
