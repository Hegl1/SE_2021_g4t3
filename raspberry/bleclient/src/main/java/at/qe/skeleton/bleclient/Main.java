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
 * Entry point for program to search for Bluetooth devices and communicate with
 * them
 */
public final class Main {
	static boolean running = true;

	private Main() {
	}

	/**
	 * This program should connect to TimeFlip devices and read the facet
	 * characteristic exposed by the devices over Bluetooth Low Energy.
	 *
	 * @param args the program arguments
	 * @throws InterruptedException if finding devices gets interrupted
	 * @see <a href=
	 *      "https://github.com/DI-GROUP/TimeFlip.Docs/blob/master/Hardware/BLE_device_commutication_protocol_v3.0_en.md"
	 *      target="_top">BLE device communication protocol v3.0</a>
	 */
	public static void main(String[] args) throws InterruptedException {
		final String findDeviceName = "TimeFlip";
		BluetoothDevice device = connectDevice("TimeFlip");

		Dice dice = new Dice(device);

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
		
		// TODO calibrate facets (coz they can be assigned randomly each time)
		// TODO read facets: handle case when same facet twice in a row
		// TODO make dice disconnect when program throws an exception during runtime
		// TODO find out when dice connection is lost
		// TODO attempt reconnect after connect is lost

		/* C R E A T E   G A M E   -   S E T U P   D I C E */

		dice.inputPassword();
		// TODO redo after every reconnect with TimeFlip dice

		// dice.getServiceUuids(); // not necessary, just extra debug info

		dice.readBatteryLevel();
		// TODO check the battery level regularly

		/* I N G A M E   -   R E A D   F A C E T S */

		dice.enableFacetsNotifications();

		// TODO ???? make the ValueNotification method "run()" call following code:
		// delete history to recognize a facet even if it's the same facet again? does
		// it work that way? or maybe accelerometer data helps? probably none of this is
		// useful at all.
		// dice.deleteHistory();

		// dice.readHistory();

		// dice.readAccelerometerData();
		// no clue how to use this yet or if it's even useful

		// get updates while game is running
		lock.lock();
		try {
			while (running) {
				cv.await();
			}
		} finally {
			lock.unlock();
		}

		device.disconnect(); // TODO make sure the device disconnects in program failure cases, too
		System.out.println("Connection closed");

	}

	/**
	 * Finds all bluetooth devices with a given device name if such devices are
	 * available.
	 * 
	 * @param findDeviceName the name of the wanted bluetooth devices
	 * @return all available bluetooth devices with the given name
	 */
	private static Set<BluetoothDevice> findBluetoothDevices(final String findDeviceName) throws InterruptedException {
		BluetoothManager manager = BluetoothManager.getBluetoothManager();

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
		manager.getDevices()
				.forEach(d -> System.out.println(d.getAddress() + " - " + d.getName() + " (" + d.getRSSI() + ")"));

		if (!findDevicesSuccess) {
			System.err.println("No " + findDeviceName + " devices found during discovery.");
			System.exit(-1);
		}

		Set<BluetoothDevice> foundDevices = findDevicesManager.getFoundDevices();
		System.out.println("Found " + foundDevices.size() + " " + findDeviceName + " device(s).");

		return foundDevices;
	}

	/**
	 * Connects to a bluetooth device with a given device name if such a device is
	 * found.
	 * 
	 * @param findDeviceName the name of the wanted bluetooth device
	 * @return a successfully connected bluetooth device with the given device name,
	 *         null otherwise
	 */
	private static BluetoothDevice connectDevice(final String findDeviceName) throws InterruptedException {
		Set<BluetoothDevice> foundDevices = findBluetoothDevices(findDeviceName);
		for (BluetoothDevice device : foundDevices) {
			System.out.println("Found " + device.getName() + " device with address " + device.getAddress()
					+ " and RSSI " + device.getRSSI());

			if (device.connect()) {
				return device;
			} else {
				System.out.println("Connection not established - trying next one");
			}
		}
		return null;
	}
}
