package at.qe.timeguess.websockDto;

public class BatteryUpdateDTO implements AbstractDTO {

	private int batLevel;

	public BatteryUpdateDTO(final int batLevel) {
		this.batLevel = batLevel;
	}

	public int getBatLevel() {
		return batLevel;
	}

}
