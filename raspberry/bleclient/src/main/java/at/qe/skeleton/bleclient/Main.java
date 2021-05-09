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
	public static void main(String[] args) {
		try {
			final String findDeviceName = "TimeFlip";
			final BluetoothDevice device = connectDevice("TimeFlip");

			if (device == null) {
				return;
			}

			Dice dice = new Dice(device);

			Lock lock = new ReentrantLock();
			Condition cv = lock.newCondition();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					running = false;
					if(device.getConnected()) {
						device.disconnect();
						System.out.println("Connection closed");
					}
					lock.lock();
					try {
						cv.signalAll();
					} finally {
						lock.unlock();
					}
				}
			});
			
			/* C R E A T E   G A M E   -   S E T U P   D I C E */

			boolean passwordSuccess = dice.inputPassword();

			if (passwordSuccess) {
				// starting a thread to check the connection status regularly
				ConnectionThread connectionThread = new ConnectionThread(dice);
				connectionThread.start();

				// starting a thread to check the battery level regularly
				BatteryThread batteryThread = new BatteryThread(dice);
				batteryThread.start();

				Thread.sleep(100);

				/* I N G A M E   -   R E A D   F A C E T S */

				dice.enableFacetsNotifications();

				// get updates while game is running
				lock.lock();
				try {
					while (running) {
						cv.await();
					}
				} finally {
					lock.unlock();
				}
			}

		//} catch (InterruptedException e) { // ?		
		} catch (Exception e) {
			e.printStackTrace(System.out);

		} 
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
				System.out.println("Connection established");
				return device;
			} else {
				System.out.println("Connection not established - trying next one");
			}
		}
		return null;
	}
}
