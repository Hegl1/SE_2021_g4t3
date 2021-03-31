package at.qe.skeleton.bleclient;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothException;
import tinyb.BluetoothManager;

import tinyb.*;
import java.util.*;
import java.time.*;

import java.util.Set;
import java.util.concurrent.locks.*;

// TODO: use logging instead of System.out/System.err

/**
 * Entry point for program to search for Bluetooth devices and communicate with them
 */
public final class Main {

    private Main() {
    }

    /**
     * This program should connect to TimeFlip devices and read the facet characteristic exposed by the devices
     * over Bluetooth Low Energy.
     *
     * @param args the program arguments
     * @throws InterruptedException if finding devices gets interrupted
     * @see <a href="https://github.com/DI-GROUP/TimeFlip.Docs/blob/master/Hardware/BLE_device_commutication_protocol_v3.0_en.md" target="_top">BLE device communication protocol v3.0</a>
     */
    public static void main(String[] args) throws InterruptedException {
        BluetoothManager manager = BluetoothManager.getBluetoothManager();

        final String findDeviceName = "TimeFlip";

        final boolean discoveryStarted = manager.startDiscovery();
        System.out.println("The discovery started: " + (discoveryStarted ? "true" : "false"));

        FindDevicesManager findDevicesManager = new FindDevicesManager(findDeviceName);
        final boolean findDevicesSuccess = findDevicesManager.findDevices(manager);

        try {
            manager.stopDiscovery();
        } catch (BluetoothException e) {
            System.err.println("Discovery could not be stopped.");
        }

        System.out.println("All found devices:");
        manager.getDevices().forEach(d -> System.out.println(d.getAddress() + " - " + d.getName() + " (" + d.getRSSI() + ")"));

        if (!findDevicesSuccess) {
            System.err.println("No " + findDeviceName + " devices found during discovery.");
            System.exit(-1);
        }

        Set<BluetoothDevice> foundDevices = findDevicesManager.getFoundDevices();
        System.out.println("Found " + foundDevices.size() + " " + findDeviceName + " device(s).");
        for (BluetoothDevice device : foundDevices) {
            System.out.println("Found " + findDeviceName + " device with address " + device.getAddress() + " and RSSI " +
                    device.getRSSI());

            if (device.connect()) {
                System.out.println("Connection established");

                /* Plan:
                 * get timeflip service
                 * get password characteristic
                 * write password (also after every disconnect)
                 * get battery service
                 * read battery level
                 * if battery low, tell central backend
                 * in timeflip service:
                 * get facets characteristic
                 * turn on notification for facets characteristic
                 * read facets (handle case when same facet twice in a row!)
                 * (do i need "command" or "command result output" characteristics?)
                 * reconnect after disconnecting
                 */
                
                /* C R E A T E   G A M E   -    S E T U P   D I C E */
                /* get all available services */
                String[] serviceUuids = device.getUUIDs();
                System.out.println("Service UUIDs available: " + serviceUuids.length);
                for (String serviceUuid : serviceUuids) {
                    System.out.println(serviceUuid);
                    // TODO find out service names of those uuids to check if timeflip and battery service are available?
                }
                
                /* input password 
                 * (hex 30 30 30 30 30 30 = ascii 6x zero) 
                 * TODO: do again after every reconnect with TimeFlip die
                 */
                String timeFlipServiceUuid = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
                BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
                if(timeFlipService != null) {
                	System.out.println("TimeFlip Service is available");
                	BluetoothGattCharacteristic passwordCharacteristic = timeFlipService.find("f1196f57-71a4-11e6-bdf4-0800200c9a66");
                    if (passwordCharacteristic != null) {
                        byte[] passwordConfig = {0x30,0x30,0x30,0x30,0x30,0x30};
                	passwordCharacteristic.writeValue(passwordConfig);
                	System.out.println("TimeFlip password should be set now");
                	// TODO check if password is set
                    } else {
                        System.out.println("TimeFlip password characteristic not found");
                    }
                	
                } else {
                	System.out.println("TimeFlip Service is not available");
                	// TODO retry connecting to device?
                }
                
                /* check battery level */
                String batteryServiceUuid = "0000180f-0000-1000-8000-00805f9b34fb";
                BluetoothGattService batteryService = device.find(batteryServiceUuid);
                if(batteryService != null) {
                	System.out.println("Battery Service is available");
                	BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.find("00002a19-0000-1000-8000-00805f9b34fb");
                    byte[] batteryLevel = batteryLevelCharacteristic.readValue();
                    int batteryLevelValue = batteryLevel[0];
                    System.out.println("Battery level: " + batteryLevelValue);
                    // TODO send value to backend
                } else {
                	System.out.println("Battery Service is not available");
                	// TODO retry connecting to device?
                }

                /* I N G A M E   -   R E A D   F A C E T S */
                
                // get facet characteristic
                String facetsCharacteristicUuid = "f1196f52-71a4-11e6-bdf4-0800200c9a66";
                BluetoothGattCharacteristic facetsCharacteristic = timeFlipService.find(facetsCharacteristicUuid);
                
                // enable notify
                if (facetsCharacteristic != null) {
                    System.out.println("facets characteristic is available");
                    try {
                        facetsCharacteristic.enableValueNotifications(new ValueNotification());
                        System.out.println("notifications should be turned on now");
                    } catch(Exception e) {
                        System.out.println("turning on notifications didn't work");
                    }
                } else {
                	System.out.println("facets characteristic is not available");
                }
                
                // get updates while game is running
                // with help from https://github.com/intel-iot-devkit/tinyb/blob/ac6d3082d06183c860eea97f451d5a92022348e0/examples/java/Notification.java#L66
                Lock lock = new ReentrantLock();
                Condition cv = lock.newCondition();
                lock.lock();
                boolean gameIsRunning = true; // TODO set this elsewhere, maybe in a thread that listens to messages from the backend?
                try {
                    while(gameIsRunning) {
                        cv.await();
                        gameIsRunning = false; // TODO decide this elsewhere, otherwise this program will run forever and the dice has to be reset before each run of the program because it wasn't disconnected from raspy
                    }
                } finally {
                    lock.unlock();
                }
                
                
                device.disconnect(); // TODO make sure the device disconnects in program failure cases, too
                System.out.println("Connection closed");
            } else {
                System.out.println("Connection not established - trying next one");
            }
        }
    }
}
