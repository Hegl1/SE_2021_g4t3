package at.qe.timeguess.tests;

import at.qe.timeguess.controllers.StatisticsController;
import at.qe.timeguess.dto.CategoryStatisticsDTO;
import at.qe.timeguess.dto.GlobalStatisticsDTO;
import at.qe.timeguess.dto.TopGamesStatisticsDTO;
import at.qe.timeguess.dto.UserStatisticsDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@SpringBootTest
public class StatisticsControllerTest {

    @Autowired
    private StatisticsController statisticsController;

    @Test
    public void testGetUserStatistics() {
        ResponseEntity<UserStatisticsDTO> response = this.statisticsController.getUserStatistics(0L);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Haskell", response.getBody().getMost_played_category().getName());
    }

    @Test
    public void testGetGlobalStatistics() {
        ResponseEntity<GlobalStatisticsDTO> response = this.statisticsController.getGlobalStatistics();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Haskell", response.getBody().getMostPlayedCategory().getName());
    }

    @Test
    public void testGetCategoryStatus() {
        ResponseEntity<List<CategoryStatisticsDTO>> response = this.statisticsController.getCategoryStats();
        Assertions.assertEquals(3, response.getBody().size());
    }

    @Test
    public void testGetTopGames() {
        ResponseEntity<List<TopGamesStatisticsDTO>> response = this.statisticsController.getTopGames();
        Assertions.assertEquals(3, response.getBody().size());
    }

    @Test
    public void testUserNotFoundException() {
        ResponseEntity<UserStatisticsDTO> response = this.statisticsController.getUserStatistics(1000L);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
