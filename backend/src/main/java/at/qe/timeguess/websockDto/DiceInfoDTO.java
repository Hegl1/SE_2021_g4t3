package at.qe.timeguess.websockDto;

public class DiceInfoDTO {
    private boolean status;
    private int level;

    public DiceInfoDTO(final boolean status, final int level) {
        this.status = status;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isStatus() {
        return status;
    }
}
