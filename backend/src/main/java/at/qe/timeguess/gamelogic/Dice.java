package gamelogic;

/**
 * Class that holds the dice mapping for a running game.
 *
 */
public class Dice {

	private int batteryPower;
	private boolean raspberryConnected;
	private int[] pointsMapping;
	private String[] activityMapping;
	private int[] durationMapping;

	/**
	 * Default constructor that initializes the dice with the default mapping.
	 */
	public Dice() {
		pointsMapping = new int[12];
		activityMapping = new String[12];
		durationMapping = new int[12];
		this.raspberryConnected = true;
		loadDefaultMapping();
	}

	/**
	 * Constructor that initializes the dice with a given mapping. All arrays
	 * provided need to be in of size 12
	 *
	 * @param pointsMapping   array that maps facet to points
	 * @param activityMapping array that maps facet to activity.
	 * @param durationMapping array that maps facet to duration.
	 */
	public Dice(final int[] pointsMapping, final String[] activityMapping, final int[] durationMapping) {
		this.pointsMapping = pointsMapping;
		this.activityMapping = activityMapping;
		this.durationMapping = durationMapping;
	}

	/**
	 * Initializes dice with default mapping.
	 */
	private void loadDefaultMapping() {
		// TODO proper default mapping.
		for (int i = 0; i < 12; i++) {
			pointsMapping[i] = i % 3 + 1;
			durationMapping[i] = ((i % 3) + 1) * 60;
		}

		activityMapping[0] = "Mime";
		activityMapping[1] = "Speak";
		activityMapping[2] = "Draw";
		activityMapping[3] = "Rhime";
		activityMapping[4] = "Mime";
		activityMapping[5] = "Speak";
		activityMapping[6] = "Draw";
		activityMapping[7] = "Rhime";
		activityMapping[8] = "Mime";
		activityMapping[9] = "Speak";
		activityMapping[10] = "Draw";
		activityMapping[11] = "Rhime";
	}

	public int getPoints(final int facet) {
		return pointsMapping[facet];
	}

	public int getDurationInSeconds(final int facet) {
		return durationMapping[facet];
	}

	public String getActivity(final int facet) {
		return activityMapping[facet];
	}

	public int[] getPointsMapping() {
		return pointsMapping;
	}

	public void setPointsMapping(final int[] pointsMapping) {
		this.pointsMapping = pointsMapping;
	}

	public String[] getActivityMapping() {
		return activityMapping;
	}

	public void setActivityMapping(final String[] activityMapping) {
		this.activityMapping = activityMapping;
	}

	public int[] getDurationMapping() {
		return durationMapping;
	}

	public void setDurationMapping(final int[] durationMapping) {
		this.durationMapping = durationMapping;
	}

	public int getBatteryPower() {
		return batteryPower;
	}

	public void setBatteryPower(final int batteryPower) {
		this.batteryPower = batteryPower;
	}

	public boolean isRaspberryConnected() {
		return raspberryConnected;
	}

	public void setRaspberryConnected(final boolean raspberryConnected) {
		this.raspberryConnected = raspberryConnected;
	}

}
