package at.qe.skeleton.bleclient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;
import tinyb.BluetoothGattCharacteristic;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;

public class DiceTest {

    @Test
    public void testDice() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        Assert.assertNotNull(dice);
        Assert.assertNotNull(dice.getDevice());
        Assert.assertNotNull(dice.getBackendCommunicator());
        Assert.assertNotNull(dice.getId());
    }

    @Test
    public void testInputPassword() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        Assert.assertNotNull(dice);
        boolean success = dice.inputPassword();
        Assert.assertTrue(success);
    }

    @Test
    public void testReadBatteryLevel() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        Assert.assertNotNull(dice);
        boolean success = dice.readBatteryLevel();
        Assert.assertTrue(success);
    }

    @Test
    public void testEnableFacetsNotifications() {
        BluetoothDevice mockDevice = utilMockDevice();
        Dice dice = new Dice(mockDevice);
        Assert.assertNotNull(dice);
        boolean success = dice.enableFacetsNotifications();
        Assert.assertTrue(success);
    }

    

    private BluetoothDevice utilMockDevice() {
        BluetoothDevice mockTimeFlip = Mockito.mock(BluetoothDevice.class);
        when(mockTimeFlip.getName()).thenReturn("timeflip");
        when(mockTimeFlip.getAddress()).thenReturn("A8:A8:9F:B9:28:AD");
        when(mockTimeFlip.getRSSI()).thenReturn((short) -42);

        BluetoothGattService mockService = utilMockService();
        when(mockTimeFlip.find(anyString())).thenReturn(mockService);
        return mockTimeFlip;
    }

    private BluetoothGattService utilMockService() {
        BluetoothGattService mockService = Mockito.mock(BluetoothGattService.class);
        BluetoothGattCharacteristic mockCharacteristic = utilMockCharacteristic();
        when(mockService.find(anyString())).thenReturn(mockCharacteristic);
        return mockService;
    }

    private BluetoothGattCharacteristic utilMockCharacteristic() {
        BluetoothGattCharacteristic mockCharacteristic = Mockito.mock(BluetoothGattCharacteristic.class);
        when(mockCharacteristic.writeValue(any(byte[].class))).thenReturn(true);
        byte[] mockData = { 0x11 };
        when(mockCharacteristic.readValue()).thenReturn(mockData);
        return mockCharacteristic;
    }
}
