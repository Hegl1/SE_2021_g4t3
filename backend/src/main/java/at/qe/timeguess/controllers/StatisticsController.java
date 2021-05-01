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

@RequestMapping("/stats")
@RestController
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/users/{id}")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(@PathVariable final Long id) {
        UserStatisticsDTO userStatisticsDTO = this.statisticsService.getUserStatistics(id);
        return new ResponseEntity<>(userStatisticsDTO, HttpStatus.OK);
    }

    @GetMapping("/global")
    public ResponseEntity<GlobalStatisticsDTO> getGlobalStatistics() {
        GlobalStatisticsDTO globalStatisticsDTO = this.statisticsService.getGlobalStatistics();
        return new ResponseEntity<>(globalStatisticsDTO, HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryStatisticsDTO>> getCategoryStats() {
        List<CategoryStatisticsDTO> categoryStatisticsDTO = this.statisticsService.getCategoryStatistics();
        return new ResponseEntity<>(categoryStatisticsDTO, HttpStatus.OK);
    }

    @GetMapping("/topGames")
    public ResponseEntity<List<TopGamesStatisticsDTO>> getTopGames() {
        List<TopGamesStatisticsDTO> topGamesStatisticsDTOs = this.statisticsService.getTopGamesStatistics();
        return new ResponseEntity<>(topGamesStatisticsDTOs, HttpStatus.OK);
    }
}
