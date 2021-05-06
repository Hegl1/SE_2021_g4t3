package at.qe.skeleton.bleclient;

import tinyb.BluetoothDevice;

public class ConnectionThread extends Thread {
    private Dice dice;
    private boolean connectionStatus;

    public ConnectionThread(Dice dice) {
        this.dice = dice;
        this.connectionStatus = true;
    }

    public void run() {
        System.out.println("Connection thread started.");
        while(true) {
            boolean oldConnectionStatus = connectionStatus;
            BluetoothDevice device = dice.getDevice(); 
            connectionStatus = device.getConnected();
			System.out.println("Connection status: " + connectionStatus);

            if (oldConnectionStatus != connectionStatus) {
                dice.getBackendCommunicator().postConnectionStatus(connectionStatus);
                // TODO if disconnected, try to reconnect
            }
            
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                System.out.println("Connection thread: sleep got interrupted.");
            }
        }
    }
}