package at.qe.timeguess.tests;

import at.qe.timeguess.dto.*;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.CompletedGame;
import at.qe.timeguess.model.CompletedGameTeam;
import at.qe.timeguess.model.User;
import at.qe.timeguess.repositories.CompletedGameRepository;
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
    private UserService userService;

    @Test
    public void testBuildCompletedGame() {
        Category category = this.categoryService.getCategoryById(0L);
        List<CompletedGameTeam> teams = new LinkedList<>();
        teams.add(new CompletedGameTeam(3, 1, 2, false));
        teams.add(new CompletedGameTeam(10, 3, 7, true));

        CompletedGame completedGame = this.statisticsService.buildCompletedGame(new Date(), new Date(), category, teams);
        Assertions.assertEquals(2, completedGame.getAttendedTeams().size());
        Assertions.assertEquals(category.getId(), completedGame.getCategory().getId());
    }

    // TODO: fix persistCompletedGame method
    //@Test
    @DirtiesContext
    public void testPersistCompletedGame() {
        Category category = this.categoryService.getCategoryById(0L);
        List<CompletedGameTeam> teams = new LinkedList<>();
        teams.add(new CompletedGameTeam(3, 1, 2, false));
        teams.add(new CompletedGameTeam(10, 3, 7, true));

        CompletedGame completedGame = this.statisticsService.persistCompletedGame(new Date(), new Date(), category, teams);
        Assertions.assertEquals(2, this.completedGameRepository.findAll().size());
    }

    @Test
    public void testGetUserStatistics() {
        UserStatisticsDTO userStatisticsDTO = this.statisticsService.getUserStatistics(0L);

        Assertions.assertEquals("Deutschland", userStatisticsDTO.getMost_played_category().getName());
        Assertions.assertEquals(1, userStatisticsDTO.getPlayed_games());
    }

    @Test
    public void testGetGlobalStatistics() {
        GlobalStatisticsDTO globalStatisticsDTO = this.statisticsService.getGlobalStatistics();

        Assertions.assertEquals(1, globalStatisticsDTO.getTotalGames());
        Assertions.assertEquals(20, globalStatisticsDTO.getNumber_correct());
        Assertions.assertEquals(5, globalStatisticsDTO.getNumber_incorrect());
        Assertions.assertEquals("Deutschland", globalStatisticsDTO.getMostPlayedCategory().getName());
        Assertions.assertTrue(globalStatisticsDTO.getMostGamesWon().stream().anyMatch(user -> user.getId().equals(0L)));
    }

    // TODO: implement properly
    @Test
    public void testGetCategoryStatistics() {
        List<CategoryStatisticsDTO> categoryStatisticsDTOs = this.statisticsService.getCategoryStatistics();

        for(CategoryStatisticsDTO categoryStatisticsDTO : categoryStatisticsDTOs) {
            System.out.println("----------------------------------");
            System.out.println("category name: " + categoryStatisticsDTO.getCategory().getName());
            System.out.println("number correct: " + categoryStatisticsDTO.getNumber_correct());
            System.out.println("number incorrect: " + categoryStatisticsDTO.getNumber_incorrect());
        }
    }

    // TODO: implement properly
    @Test
    public void testGetTopGamesStatistics() {
        List<TopGamesStatisticsDTO> topGamesStatisticsDTOs = this.statisticsService.getTopGamesStatistics();

        for(TopGamesStatisticsDTO topGamesStatisticsDTO : topGamesStatisticsDTOs) {

            System.out.println("----------------------------------");
            for(TeamStatisticsDTO teamStatisticsDTO : topGamesStatisticsDTO.getTeams()) {
                System.out.println("score: " + teamStatisticsDTO.getScore());
                System.out.println("correct: " + teamStatisticsDTO.getNumber_correct());
                System.out.println("incorrect: " + teamStatisticsDTO.getNumber_incorrect());
            }
            System.out.println("category: " + topGamesStatisticsDTO.getCategory().getName());
            System.out.println("score per time: " + topGamesStatisticsDTO.getScore_per_time());
            System.out.println("duration: " + topGamesStatisticsDTO.getDuration());
        }
    }
}
