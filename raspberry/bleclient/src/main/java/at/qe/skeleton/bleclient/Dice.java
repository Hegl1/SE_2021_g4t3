package at.qe.skeleton.bleclient;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;
import tinyb.BluetoothGattCharacteristic;

import tinyb.*;

public final class Dice {
    private BluetoothDevice device;
    private static final String timeFlipServiceUuid = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
    private static final String passwordCharacteristicUuid = "f1196f57-71a4-11e6-bdf4-0800200c9a66";
    private static final byte[] passwordConfig = {0x30,0x30,0x30,0x30,0x30,0x30};

    public Dice(BluetoothDevice device) {
        this.device = device;
    }

    public boolean inputPassword() {
        /* input password 
                 * (hex 30 30 30 30 30 30 = ascii 6x zero) 
                 * TODO: do again after every reconnect with TimeFlip die
                 */
                
                BluetoothGattService timeFlipService = device.find(timeFlipServiceUuid);
                if(timeFlipService != null) {
                	System.out.println("TimeFlip Service is available");
                	BluetoothGattCharacteristic passwordCharacteristic = timeFlipService.find(passwordCharacteristicUuid);
                    if (passwordCharacteristic != null) {
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
                return true; // TODO check this 
    }
}