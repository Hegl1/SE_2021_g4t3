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

// this program was written with help from https://github.com/intel-iot-devkit/tinyb/blob/ac6d3082d06183c860eea97f451d5a92022348e0/examples/java/Notification.java#L66
// a note from the Author of the code from the link above:
/*
 * Author: Petre Eftime <petre.p.eftime@intel.com>
 * Copyright (c) 2016 Intel Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

// TODO: use logging instead of System.out/System.err ?

/**
 * Entry point for program to search for Bluetooth devices and communicate with them
 */
public final class Main {
    static boolean running = true;
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

                Lock lock = new ReentrantLock();
                Condition cv = lock.newCondition();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        running = false;
                        lock.lock();
                        try {
                            cv.signalAll();
                        } finally {
                            lock.unlock();
                        }
                    }
                });

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

                // TODO ???? make the ValueNotification method "run()" call following code:
                // delete history to recognize a facet even if it's the same facet again? does it work that way? or maybe accelerometer data helps?
                String commandCharacteristicUuid = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
                BluetoothGattCharacteristic commandCharacteristic = timeFlipService.find(commandCharacteristicUuid);
                if (commandCharacteristic != null) {
                    System.out.println("command characteristic is available");
                    System.out.println("deleting history");
                    byte[] deleteHistoryConfig = {0x02};
                    commandCharacteristic.writeValue(deleteHistoryConfig);
                    byte[] result = commandCharacteristic.readValue(); // check if write request worked
                    for (byte b : result) {
                        System.out.print(String.format("%02x ", b)); // if output is "02 02" it's "OK"
                    }
                    System.out.println(""); 
                } else {
                    System.out.println("command characteristic is not available");
                }

                // read history
                if (commandCharacteristic != null) {
                    System.out.println("command characteristic is available");
                    System.out.println("reading history");
                    byte[] readHistoryConfig = {0x01};
                    commandCharacteristic.writeValue(readHistoryConfig);
            
                    String commandResultOutputCharacteristicUuid = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
                    BluetoothGattCharacteristic commandResultOutputCharacteristic = timeFlipService.find(commandResultOutputCharacteristicUuid);
                    if (commandResultOutputCharacteristic != null) {
                        System.out.println("command result output characteristic is available");
                        for (int i = 0; i < 5; i++) {
                            byte[] historyLine = commandResultOutputCharacteristic.readValue(); // reads just one of multible lines. last line is just zeros. TODO put this in a loop that recognizes last line
                            for (byte b : historyLine) {
                                // TODO research: what exactly do I get from the byte? hex value? asci code?
                                System.out.print(String.format("%02x ", b)); 
                            }
                            System.out.println(""); 
                        }
                        // TODO how to interpret the history???
                        // why is it not empty after i deleted history?
                    } else {
                        System.out.println("command result output characteristic is not available");
                    }
                } else {
                    System.out.println("command characteristic is not available");
                }

                // read accelerometer data - no clue how to use this yet or even if it's useful
                String accelerometerDataCharacteristicUuid = "f1196f51-71a4-11e6-bdf4-0800200c9a66";
                BluetoothGattCharacteristic accelerometerDataCharacteristic = timeFlipService.find(accelerometerDataCharacteristicUuid);
                if (accelerometerDataCharacteristic != null) {
                    System.out.println("accelerometer data characteristic is available");
                    byte[] result = accelerometerDataCharacteristic.readValue();
                    for (byte b : result) {
                        System.out.print(String.format("%02x ", b)); 
                    }
                    System.out.println(""); 
                } else {
                    System.out.println("accelerometer data characteristic is not available");
                }
                
                // get updates while game is running
                lock.lock();
                try {
                    while(running) {
                        cv.await();
                        running = false;
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
