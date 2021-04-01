package at.qe.skeleton.bleclient;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothException;
import tinyb.BluetoothManager;

import java.util.Set;

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
                // TODO: read from device
                
                String[] uuids = device.getUUIDs();
                System.out.println("UuidsArraySize: " + uuids.length);
                for (String current : uuids) {
                    System.out.println("getUuid: " + current);
                }
                
                System.out.println("\ngetServices: " + device.getServices() + " size: " + device.getServices().size());
                System.out.println("getServiceData: " + device.getServiceData() + " size: " + device.getServiceData().size());
                
                BluetoothGattService bgs = device.find("f1196f50-71a4-11e6-bdf4-0800200c9a66");
                if(bgs != null) {
                    BluetoothGattCharacteristic characteristic = bgs.find("f1196f57-71a4-11e6-bdf4-0800200c9a66");
                    byte[] config = {0x30,0x30,0x30,0x30,0x30,0x30};
                    characteristic.writeValue(config);
                    System.out.println("Password set");
                    // System.out.println("getPassword: " + characteristic.readValue());
                } else {
                    System.out.println("Service is null");
                }
                
                device.disconnect();
            } else {
                System.out.println("Connection not established - trying next one");
            }
        }
    }
}
