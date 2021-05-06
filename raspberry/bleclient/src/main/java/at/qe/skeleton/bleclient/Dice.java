package at.qe.skeleton.bleclient;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;
import tinyb.BluetoothGattCharacteristic;

import tinyb.*;

/**
 * Class that contains methods needed for reading values from and writing values
 * into a TimeFlip dice.
 */
public final class Dice {
	private String id;
	private BluetoothDevice device;
	private BackendCommunicator backendCommunicator;

	private static final String batteryServiceUuid = "0000180f-0000-1000-8000-00805f9b34fb";
	private static final String timeFlipServiceUuid = "f1196f50-71a4-11e6-bdf4-0800200c9a66";

	private static final String batteryLevelCharacteristicUuid = "00002a19-0000-1000-8000-00805f9b34fb";
	private static final String accelerometerDataCharacteristicUuid = "f1196f51-71a4-11e6-bdf4-0800200c9a66";
	private static final String facetsCharacteristicUuid = "f1196f52-71a4-11e6-bdf4-0800200c9a66";
	private static final String commandResultOutputCharacteristicUuid = "f1196f53-71a4-11e6-bdf4-0800200c9a66";
	private static final String commandCharacteristicUuid = "f1196f54-71a4-11e6-bdf4-0800200c9a66";
	private static final String calibrationVersionCharacteristicUuid = "f1196f56-71a4-11e6-bdf4-0800200c9a66";
	private static final String passwordCharacteristicUuid = "f1196f57-71a4-11e6-bdf4-0800200c9a66";

	private static final byte[] passwordConfig = { 0x30, 0x30, 0x30, 0x30, 0x30, 0x30 };

	public Dice(BluetoothDevice device) {
		this.device = device;
		this.backendCommunicator = new BackendCommunicator();
		this.id = this.backendCommunicator.getDiceId();
	}

	public BluetoothDevice getDevice() {
		return this.device;
	}

	public BackendCommunicator getBackendCommunicator() {
		return this.backendCommunicator;
	}

	public String getId() {
		return this.id;
	}

	/**
	 * Inputs the password into the TimeFlip dice. This is necessary to be able to
	 * read further values from the TimeFlip service's characteristics. TimeFlip
	 * requires password input after re-connect to authorize connected device. The
	 * default password is ASCII "000000" or {0x30, 0x30, 0x30, 0x30, 0x30, 0x30}.
	 * 
	 * @return true if password has been written into the dice, false otherwise
	 */
	public boolean inputPassword() {
		BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
		if (timeFlipService != null) {
			BluetoothGattCharacteristic passwordCharacteristic = timeFlipService.find(passwordCharacteristicUuid);
			if (passwordCharacteristic != null) {
				boolean success = passwordCharacteristic.writeValue(passwordConfig);
				if (success) {
					System.out.println("TimeFlip password input successful");
					return success;
				}
			} 
		} 
		System.out.println("TimeFlip password input failed");
		return false;
	}

	/**
	 * Reads the battery level from the TimeFlip dice. The battery level can be any
	 * number from 0 to 100.
	 * 
	 * @return true if battery level was read, false otherwise
	 */
	public boolean readBatteryLevel() {
		BluetoothGattService batteryService = device.find(batteryServiceUuid);
		if (batteryService != null) {
			BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService
					.find(batteryLevelCharacteristicUuid);
			byte[] batteryLevel = batteryLevelCharacteristic.readValue();
			int batteryLevelValue = Byte.toUnsignedInt(batteryLevel[0]);
			System.out.println("Battery level: " + batteryLevelValue);
			backendCommunicator.postBatteryStatus(batteryLevelValue);
			return true;
		} 
		return false;
	}

	/**
	 * Finds out which services are available on the device and returns their UUIDs.
	 * 
	 * @return all available service UUIDs
	 *//*
	public String[] getServiceUuids() {
		String[] serviceUuids = device.getUUIDs();
		System.out.println("Service UUIDs available: " + serviceUuids.length);
		for (String serviceUuid : serviceUuids) {
			System.out.println(serviceUuid);
		}
		return serviceUuids;
	}*/

	/**
	 * Enables notifications for when the TimeFlip dice lands on a new facet after
	 * being rolled or tilted.
	 * 
	 * @return true if notifications were enabled, false otherwise
	 */
	public boolean enableFacetsNotifications() {
		BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
		if (timeFlipService != null) {
			BluetoothGattCharacteristic facetsCharacteristic = timeFlipService.find(facetsCharacteristicUuid);
			if (facetsCharacteristic != null) {
				try {
					facetsCharacteristic.enableValueNotifications(new ValueNotification(this.backendCommunicator));
					System.out.println("notifications should be turned on now");
					return true;
				} catch (Exception e) {
					System.out.println("turning on notifications didn't work");
				}
			} 
		}
		return false;
	}

	/**
	 * Deletes the history of the TimeFlip dice.
	 * 
	 * @return true if history got deleted, false otherwise
	 *//*
	public boolean deleteHistory() {
		BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
		BluetoothGattCharacteristic commandCharacteristic = timeFlipService.find(commandCharacteristicUuid);
		if (commandCharacteristic != null) {
			// System.out.println("command characteristic is available");
			System.out.println("deleting history");
			byte[] deleteHistoryConfig = { 0x02 };
			commandCharacteristic.writeValue(deleteHistoryConfig);
			byte[] result = commandCharacteristic.readValue(); // check if write request worked
			for (byte b : result) {
				System.out.print(String.format("%02x ", b)); // if output is "02 02" it's "OK"
			}
			System.out.println("");
			return true;
		} else {
			// System.out.println("command characteristic is not available");
		}
		return false;
	}*/

	/**
	 * Read the history from the TimeFlip dice.
	 * 
	 * @return true if history was read out, false otherwise
	 *//*
	public boolean readHistory() {
		BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
		BluetoothGattCharacteristic commandCharacteristic = timeFlipService.find(commandCharacteristicUuid);
		if (commandCharacteristic != null) {
			// System.out.println("command characteristic is available");
			System.out.println("reading history");
			byte[] readHistoryConfig = { 0x01 };
			commandCharacteristic.writeValue(readHistoryConfig);

			BluetoothGattCharacteristic commandResultOutputCharacteristic = timeFlipService
					.find(commandResultOutputCharacteristicUuid);
			if (commandResultOutputCharacteristic != null) {
				// System.out.println("command result output characteristic is available");
				for (int i = 0; i < 5; i++) {
					byte[] historyLine = commandResultOutputCharacteristic.readValue(); // reads just one of multible
																						// lines. last line is just
																						// zeros. TODO put this in a
																						// loop that recognizes last
																						// line
					for (byte b : historyLine) {
						// TODO research: what exactly do I get from the byte? hex value? asci code?
						System.out.print(String.format("%02x ", b));
					}
					System.out.println("");
				}
				return true;
				// TODO how to interpret the history???
				// why is it not empty after i deleted history?
			} else {
				// System.out.println("command result output characteristic is not available");
			}
		} else {
			// System.out.println("command characteristic is not available");
		}
		return false;
	}*/

	/**
	 * Reads the accelerometer data from the TimeFlip dice.
	 * 
	 * @return true if the data was read out, false otherwise
	 */
	/*public boolean readAccelerometerData() {
		BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
		BluetoothGattCharacteristic accelerometerDataCharacteristic = timeFlipService
				.find(accelerometerDataCharacteristicUuid);
		if (accelerometerDataCharacteristic != null) {
			// System.out.println("accelerometer data characteristic is available");
			byte[] result = accelerometerDataCharacteristic.readValue();
			for (byte b : result) {
				System.out.print(String.format("%02x ", b));
			}
			System.out.println("");
			return true;
		} else {
			// System.out.println("accelerometer data characteristic is not available");
		}
		return false;
	}*/
	/*
	public boolean readCalibrationVersion() {
		BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
		BluetoothGattCharacteristic calibrationVersionCharacteristic = timeFlipService
				.find(calibrationVersionCharacteristicUuid);
		if (calibrationVersionCharacteristic != null) {
			System.out.println("calibration version characteristic is available");
			byte[] result = calibrationVersionCharacteristic.readValue();
			for (byte b : result) {
				System.out.print(String.format("%02x ", b));
			}
			System.out.println("");
			return true;
		} else {
			System.out.println("calibration version characteristic is not available");
		}
		return false;
	}

	public boolean setCalibrationVersion() {
		BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
		BluetoothGattCharacteristic calibrationVersionCharacteristic = timeFlipService
				.find(calibrationVersionCharacteristicUuid);
		if (calibrationVersionCharacteristic != null) {
			byte[] calibrationConfig = { 0x31, 0x31, 0x31, 0x31}; 
			// random example just to see if that fixes anything - it doesn't. battery change fucks up everything
			calibrationVersionCharacteristic.writeValue(calibrationConfig);
			System.out.println("TimeFlip calibration input done");
			return true;
		} else {
			System.out.println("calibration version characteristic is not available");
		}
		return false;
	}
	*/
}