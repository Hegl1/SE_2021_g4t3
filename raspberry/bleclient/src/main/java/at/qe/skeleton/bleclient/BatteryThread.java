package at.qe.skeleton.bleclient;

/**
 * The BatteryThread class provides a method to regularly check the battery status after the thread is started.
 */
public class BatteryThread extends Thread {
    private Dice dice;

    public BatteryThread(Dice dice) {
        this.dice = dice;
    }

    /**
	 * Method that runs after the thread is started. The battery status is being checked regularly.
	 */
    public void run() {
        System.out.println("Battery thread started.");
        while(true) {
            dice.readBatteryLevel(); // HTTP requests to backend are included
            try {
                Thread.sleep(1200000); // 20 minutes
            } catch (InterruptedException e) {
                System.out.println("Battery thread: sleep got interrupted.");
            }
        }
    }
}