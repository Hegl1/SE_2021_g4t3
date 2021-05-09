package at.qe.timeguess.tests;

import at.qe.timeguess.dto.*;
import at.qe.timeguess.gamelogic.Team;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.model.CompletedGameTeam;
import at.qe.timeguess.repositories.CompletedGameRepository;
import at.qe.timeguess.repositories.CompletedGameTeamRepository;
import at.qe.timeguess.services.CategoryService;
import at.qe.timeguess.services.StatisticsService;
import at.qe.timeguess.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
public class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CompletedGameRepository completedGameRepository;

    @Autowired
    private CompletedGameTeamRepository completedGameTeamRepository;

    @Autowired
    private UserService userService;

    @Test
    public void testBuildCompletedGame() {
        Category category = this.categoryService.getCategoryById(0L);
        List<Team> teams = new LinkedList<>();
        teams.add(new Team());
        teams.add(new Team());

        CompletedGame completedGame = this.statisticsService.buildCompletedGame(new Date(), new Date(), category, teams);
        Assertions.assertEquals(2, completedGame.getAttendedTeams().size());
        Assertions.assertEquals(category.getId(), completedGame.getCategory().getId());
    }

    @Test
    @DirtiesContext
    public void testPersistCompletedGame() {
        Category category = this.categoryService.getCategoryById(0L);
        List<Team> teams = new LinkedList<>();
        teams.add(new Team());
        teams.add(new Team());

        CompletedGame completedGame = this.statisticsService.persistCompletedGame(new Date(), new Date(), category, teams);
        Assertions.assertEquals(4, this.completedGameRepository.findAll().size());
        Assertions.assertEquals(8, this.completedGameTeamRepository.findAll().size());
    }

    @Test
    public void testGetUserStatistics() throws StatisticsService.UserNotFoundException {
        UserStatisticsDTO userStatisticsDTO = this.statisticsService.getUserStatistics(0L);

        Assertions.assertEquals("Haskell", userStatisticsDTO.getMost_played_category().getName());
        Assertions.assertEquals(2, userStatisticsDTO.getPlayed_games());
    }

    @Test
    public void testGetGlobalStatistics() {
        GlobalStatisticsDTO globalStatisticsDTO = this.statisticsService.getGlobalStatistics();

        Assertions.assertEquals(3, globalStatisticsDTO.getTotalGames());
        Assertions.assertEquals(80, globalStatisticsDTO.getNumber_correct());
        Assertions.assertEquals(70, globalStatisticsDTO.getNumber_incorrect());
        Assertions.assertEquals("Haskell", globalStatisticsDTO.getMostPlayedCategory().getName());
        Assertions.assertTrue(globalStatisticsDTO.getMostGamesWon().stream().anyMatch(user -> user.getId().equals(1L)));
    }

    @Test
    public void testGetCategoryStatistics() {
        List<CategoryStatisticsDTO> categoryStatisticsDTOs = this.statisticsService.getCategoryStatistics();
        Assertions.assertEquals(8, categoryStatisticsDTOs.size());
    }

    @Test
    public void testGetTopGamesStatistics() {
        List<TopGamesStatisticsDTO> topGamesStatisticsDTOs = this.statisticsService.getTopGamesStatistics();
        Assertions.assertEquals(3, topGamesStatisticsDTOs.size());
    }

    @Test
    public void testUserNotFoundException() {
        Assertions.assertThrows(StatisticsService.UserNotFoundException.class, () -> this.statisticsService.getUserStatistics(1000L));
    }
}
