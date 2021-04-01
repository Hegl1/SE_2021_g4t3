package at.qe.skeleton.bleclient;

import com.google.common.base.Preconditions;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

// TODO: use logging instead of System.out

// TODO: add variable such as 'running' to stop searching earlier in a graceful way e.g. when using signal handling

/**
 * A class for found Bluetooth devices
 */
public class FindDevicesManager {

    public static final int MIN_RSSI_ALLOWED = -80;

    private static final int ATTEMPTS_TO_FIND = 5;

    private static final long SECONDS_BETWEEN_ATTEMPTS = 4L;

    private final String searchDevice;

    private final Set<BluetoothDevice> foundDevices = new HashSet<>();

    public FindDevicesManager(final String searchDeviceName) {
        Preconditions.checkNotNull(searchDeviceName, "Precondition violation - argument 'searchDeviceName' must not be NULL!");
        this.searchDevice = searchDeviceName;
    }

    // TODO: could search for devices based on information from server e.g. at least two devices
    //  with a certain ID should be reachable from this machine based on MAC address

    /**
     * Search for Bluetooth devices until either at least one device could be found or until
     * we reached the maximum number of attempts. We sleep a certain amount of time after each attempt.
     * We need to we search long enough otherwise we may not find the device even though it is there.
     *
     * @param manager the Bluetooth manager from which we get the Bluetooth devices
     * @return true if at least one device could be found
     * @throws InterruptedException if sleep between attempts gets interrupted
     */
    public boolean findDevices(final BluetoothManager manager) throws InterruptedException {
        Preconditions.checkNotNull(manager, "Precondition violation - argument 'manager' must not be NULL!");

        for (int i = 0; i < ATTEMPTS_TO_FIND; i++) {
            List<BluetoothDevice> devices = manager.getDevices();
            if (devices == null) {
                return false;
            }
            for (BluetoothDevice device : devices) {
                checkDevice(device);
            }
            if (!this.foundDevices.isEmpty()) {
                return true;
            }
            TimeUnit.SECONDS.sleep(SECONDS_BETWEEN_ATTEMPTS);
        }
        return false;
    }

    /**
     * Check if the provided device is one we are searching for and add it to found devices
     * Filter based on searchDeviceName and RSSI signal to ensure that we connect to the
     * correct device and to prevent unstable communication.
     *
     * @param device the device to check
     */
    private void checkDevice(final BluetoothDevice device) {
        Preconditions.checkNotNull(device, "Precondition violation - argument 'device' must not be NULL!");

        if (device.getName().toLowerCase(Locale.ROOT).contains(this.searchDevice.toLowerCase(Locale.ROOT))) {
            final int rssi = device.getRSSI();
            if (rssi == 0) {
                System.out.println(this.searchDevice + " with address " + device.getAddress() + " has no signal.");
            } else if (rssi < MIN_RSSI_ALLOWED) {
                System.out.println(this.searchDevice + " with address " + device.getAddress() + " has a very low signal ("
                        + rssi + ")");
            } else {
                this.foundDevices.add(device);
            }
        }
    }

    public Set<BluetoothDevice> getFoundDevices() {
        return Collections.unmodifiableSet(this.foundDevices);
    }
}
