package at.qe.skeleton.bleclient;

import tinyb.BluetoothDevice;

/**
 * The ConnectionThread class provides a method to regularly check the connection status after the thread is started.
 */
public class ConnectionThread extends Thread {
    private Dice dice;
    private boolean connected;
    private static final int ATTEMPTS_TO_FIND = 18;
    private static final long MILLISECONDS_BETWEEN_ATTEMPTS = 10000L;

    public ConnectionThread(Dice dice) {
        this.dice = dice;
        this.connected = true;
    }

    /**
	 * Method that runs after the thread is started. The connection status is being checked regularly.
     * If the bluetooth device has no connection the method tries to reconnect.
	 */
    public void run() {
        System.out.println("Connection thread started");
        while(true) {
            boolean oldConnected = connected;
            BluetoothDevice device = dice.getDevice(); 
            connected = device.getConnected();
			//System.out.println("Connected: " + connected);

            if (oldConnected != connected) {
                //System.out.println("Connection status changed");
                dice.getBackendCommunicator().postConnectionStatus(connected);
                if (connected) {
                    System.out.println("Connection re-established");
                } else {
                    System.out.println("Connection lost");
                    boolean reconnected = false;
                    while (!reconnected) {
                        reconnected = reconnectDevice(device);
                        if (!reconnected) {
                            System.out.println("Attempts to reconnect failed. Trying again in 3 minutes");
                            try {
                                Thread.sleep(180000); // 3 minutes
                            } catch (InterruptedException e) {
                                System.out.println("Connection thread: sleep got interrupted.");
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Connection thread: sleep got interrupted.");
                    }
                    dice.setFreshlyReconnected(true);
                    dice.inputPassword();
                }
            }
            
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                System.out.println("Connection thread: sleep got interrupted.");
            }
        }
    }

    /**
	 * Tries to reconnect a disconnected bluetooth device, i.e. the Timeflip dice.
	 * 
	 * @param device the disconnected bluetooth device
     * @return true if reconnecting was successful, false otherwise
	 */
    public boolean reconnectDevice(BluetoothDevice device) {
        System.out.println("Trying to reconnect to the TimeFlip device");
        boolean reconnected = false;
        for (int i = 1; i <= ATTEMPTS_TO_FIND; i++) {
            try {
                if(device.connect()) {
                    System.out.println("Attempt " + i + " successful");
                    return true;
                } 
            } catch (Exception e) {
                System.out.println("Attempt " + i + " failed");
                try {
                    //System.out.println("Connection thread: sleeping.");
                    Thread.sleep(MILLISECONDS_BETWEEN_ATTEMPTS);
                } catch (InterruptedException ie) {
                    System.out.println("Connection thread: sleep got interrupted.");
                }
            }
        }
        return false;
    }
}