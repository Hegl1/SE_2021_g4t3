package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.CategoryStatisticsDTO;
import at.qe.timeguess.dto.GlobalStatisticsDTO;
import at.qe.timeguess.dto.TopGamesStatisticsDTO;
import at.qe.timeguess.dto.UserStatisticsDTO;
import at.qe.timeguess.services.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Class that controls the retrieving of Statistics of Categories, Players or Games
 *
 */
@RequestMapping("/stats")
@RestController
public class StatisticsController {

    // TODO: check endpoints

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Retrieves Statistics of a User
     *
     * @param id the ID of the User to get the Statistics for
     * @return ResponseEntity for REST communication:
     *      code OK if successful
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(@PathVariable final Long id) {
        UserStatisticsDTO userStatisticsDTO = this.statisticsService.getUserStatistics(id);
        return new ResponseEntity<>(userStatisticsDTO, HttpStatus.OK);
    }

    /**
     * Retrieves global Statistics
     *
     * @return ResponseEntity for REST communication:
     *      code OK if successful
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalStatisticsDTO> getGlobalStatistics() {
        GlobalStatisticsDTO globalStatisticsDTO = this.statisticsService.getGlobalStatistics();
        return new ResponseEntity<>(globalStatisticsDTO, HttpStatus.OK);
    }

    /**
     * Retrieves Statistics of all Categories
     *
     * @return ResponseEntity for REST communication:
     *      code OK if successful
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryStatisticsDTO>> getCategoryStats() {
        List<CategoryStatisticsDTO> categoryStatisticsDTO = this.statisticsService.getCategoryStatistics();
        return new ResponseEntity<>(categoryStatisticsDTO, HttpStatus.OK);
    }

    /**
     * Retrieves Statistics of the top best Games
     *
     * @return ResponseEntity for REST communication:
     *      code OK if successful
     */
    @GetMapping("/topGames")
    public ResponseEntity<List<TopGamesStatisticsDTO>> getTopGames() {
        List<TopGamesStatisticsDTO> topGamesStatisticsDTOs = this.statisticsService.getTopGamesStatistics();
        return new ResponseEntity<>(topGamesStatisticsDTOs, HttpStatus.OK);
    }
}
