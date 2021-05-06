package at.qe.timeguess.dto;

public class TeamStatisticsDTO {

    private int score;
    private int number_correct;
    private int number_incorrect;

    public TeamStatisticsDTO() {
    }

    public TeamStatisticsDTO(int score, int number_correct, int number_incorrect) {
        this.score = score;
        this.number_correct = number_correct;
        this.number_incorrect = number_incorrect;
    }

    public int getScore() {
        return score;
    }

    public int getNumber_correct() {
        return number_correct;
    }

    public int getNumber_incorrect() {
        return number_incorrect;
    }
}
