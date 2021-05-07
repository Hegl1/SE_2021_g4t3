package at.qe.timeguess.websockDto;

import at.qe.timeguess.dto.GameDTO;
import at.qe.timeguess.dto.TeamDTO;
import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.Category;
import at.qe.timeguess.model.User;

import java.util.List;

public class StateUpdateDTO extends GameDTO implements AbstractDTO {

    private String status;
    private WaitingDataDTO waiting_data;
    private RunningDataDTO running_data;
    private DiceInfoDTO dice_info;


    public StateUpdateDTO(final String status, final WaitingDataDTO waiting_data, final RunningDataDTO running_data, final DiceInfoDTO dice_info,
                          final int code, final List<TeamDTO> teams, final User host, final Category category, final int max_score) {
        super(code, teams, new UserDTO(host.getId(), host.getUsername(), host.getRole().toString()), category,
            max_score);
        this.status = status;
        this.waiting_data = waiting_data;
        this.running_data = running_data;
        this.dice_info = dice_info;
    }

    public String getStatus() {
        return status;
    }

    public WaitingDataDTO getWaiting_data() {
        return waiting_data;
    }

    public RunningDataDTO getRunning_data() {
        return running_data;
    }

    public DiceInfoDTO getDice_info() {
        return dice_info;
    }
}
