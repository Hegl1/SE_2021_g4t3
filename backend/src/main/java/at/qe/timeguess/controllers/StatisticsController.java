package at.qe.timeguess.controllers;

import at.qe.timeguess.dto.UserStatisticsDTO;
import at.qe.timeguess.services.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/stats")
@RestController
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/user/{id}")
    public void getUserStatistics(@PathVariable final Long id) {
        this.statisticsService.getUserStatistics(id);
    }

    @GetMapping("/global")
    public void getGlobalStatistics() {
        this.statisticsService.getGlobalStatistics();
    }

    @GetMapping("/categories")
    public void getCategoryStats() {
        this.statisticsService.getCategoryStatistics();
    }

    @GetMapping("/topGames")
    public void getTopGames() {
        this.statisticsService.getTopGamesStatistics();
    }
}
