package at.qe.skeleton.bleclient;

public class BatteryThread extends Thread {
    private Dice dice;

    public BatteryThread(Dice dice) {
        this.dice = dice;
    }

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